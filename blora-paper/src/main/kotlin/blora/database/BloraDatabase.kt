package blora.database

import blora.configuration.CONF
import blora.database.mail.dao.MailDao
import blora.database.mail.dao.SystemMailDao
import blora.database.mail.table.MailTable
import blora.database.mail.table.SystemMailTable
import blora.database.player.dao.PlayerDao
import blora.database.player.dao.PlayerInfoDao
import blora.database.player.dao.PlayerLoginDao
import blora.database.player.dao.PlayerOnlineDataDao
import blora.database.player.table.PlayerInfoTable
import blora.database.player.table.PlayerLoginTable
import blora.database.player.table.PlayerOnlineDataTable
import blora.database.player.table.PlayerTable
import blora.database.redeem.dao.PlayerRedeemDao
import blora.database.redeem.dao.RedeemDao
import blora.database.redeem.table.PlayerRedeemTable
import blora.database.redeem.table.RedeemTable
import blora.mail.CreatingMailContext
import blora.mail.Sender
import blora.player.PlayerLoginData
import blora.player.PlayerOnlineData
import blora.plugin.BloraPlugin
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

val DB: BloraDatabase
    get() = BloraPlugin.database

class BloraDatabase(
    private var dataSource: HikariDataSource,
) {

    var database = Database.connect(dataSource)

    private val keepAliveJob: BukkitTask

    init {
        this.keepAliveJob = Bukkit.getScheduler()
            .runTaskTimer(
                BloraPlugin,
                Runnable {
                    if (this.dataSource.isClosed) {
                        this.dataSource.close() // ensure close again
                        this.dataSource = CONF.database.buildDataSource()
                        this.database = Database.connect(this.dataSource)
                    }
                },
                10 * 20L,
                10 * 20L // every 10 seconds check once
            )
    }

    fun <T> trans(statement: Transaction.() -> T): T {
        return transaction(database, statement)
    }

    fun initTables() {
        trans {
            // Player
            SchemaUtils.create(PlayerTable)
            SchemaUtils.create(PlayerLoginTable)
            SchemaUtils.create(PlayerInfoTable)
            SchemaUtils.create(PlayerOnlineDataTable)
            // Mail
            SchemaUtils.create(MailTable)
            SchemaUtils.create(SystemMailTable)
            // Redeem
            SchemaUtils.create(RedeemTable)
            SchemaUtils.create(PlayerRedeemTable)

            // Existing DBs may already have rows that would block new unique indexes.
            // Prefer "better" rows where possible (visible / actived), else oldest id.
            // Dedupe + index creation share this transaction: any failure rolls both back.
            dedupeBeforeUniqueIndexes()

            // Applies newly declared unique indexes / missing columns on existing tables.
            SchemaUtils.createMissingTablesAndColumns(
                PlayerTable,
                PlayerLoginTable,
                PlayerInfoTable,
                PlayerOnlineDataTable,
                MailTable,
                SystemMailTable,
                RedeemTable,
                PlayerRedeemTable,
            )
        }
    }

    /**
     * Removes historical duplicates so unique indexes can be created safely.
     * Runs inside [initTables]'s transaction — failure rolls back create + dedupe together.
     */
    private fun Transaction.dedupeBeforeUniqueIndexes() {
        // blora_player_used_redeems (player, code)
        exec(
            """
            DELETE FROM blora_player_used_redeems a
            USING blora_player_used_redeems b
            WHERE a.player = b.player
              AND a.code = b.code
              AND a.id > b.id
            """.trimIndent()
        )
        // blora_redeem_codes (code)
        exec(
            """
            DELETE FROM blora_redeem_codes a
            USING blora_redeem_codes b
            WHERE a.code = b.code
              AND a.id > b.id
            """.trimIndent()
        )
        // blora_system_mails (identifier) — prefer actived=true, then lower id
        exec(
            """
            DELETE FROM blora_system_mails a
            USING blora_system_mails b
            WHERE a.identifier = b.identifier
              AND (
                    (a.actived = false AND b.actived = true)
                 OR (a.actived = b.actived AND a.id > b.id)
              )
            """.trimIndent()
        )
        // blora_mails (receiver, system_mail_id) — only non-null system mails; nulls stay multi-row in PG
        // Prefer visible=true, then lower id
        exec(
            """
            DELETE FROM blora_mails a
            USING blora_mails b
            WHERE a.system_mail_id IS NOT NULL
              AND b.system_mail_id IS NOT NULL
              AND a.receiver = b.receiver
              AND a.system_mail_id = b.system_mail_id
              AND (
                    (a.visible = false AND b.visible = true)
                 OR (a.visible = b.visible AND a.id > b.id)
              )
            """.trimIndent()
        )
    }

    // Player Login

    fun getPlayerLoginData(uuid: UUID): PlayerLoginData {
        return trans {
            PlayerLoginData(
                uuid,
                PlayerLoginDao.find {
                    PlayerLoginTable.uuid eq uuid
                }.map {
                    it.date
                }.toList()
            )
        }
    }

    // Redeem

    fun listRedeemCodes(): List<RedeemDao> {
        return trans {
            RedeemDao.all().toList()
        }
    }

    fun getRedeemByCode(code: String): RedeemDao? {
        return trans {
            RedeemDao.find {
                RedeemTable.code eq code
            }.firstOrNull()
        }
    }

    /**
     * Atomically consumes a redeem code and creates the reward mail in one transaction.
     * Callers must only notify the player on [blora.redeem.RedeemResult.Success].
     */
    fun redeemCode(
        player: UUID,
        redeem: String,
        mailTitle: String,
        mailContents: String,
        mailSenderName: String,
    ): blora.redeem.RedeemResult {
        val codeKey = redeem.lowercase()
        return try {
            trans {
                if (!tryAcquireRewardLock(player, "redeem", codeKey)) {
                    BloraPlugin.slF4JLogger.warn(
                        "[SECURITY] Concurrent redeem rejected for $player code=$codeKey"
                    )
                    return@trans blora.redeem.RedeemResult.Failed
                }

                val code = RedeemDao.find {
                    RedeemTable.code eq codeKey
                }.firstOrNull()
                if (code == null) {
                    return@trans blora.redeem.RedeemResult.NotFound
                }

                val alreadyUsed = !PlayerRedeemDao.find {
                    PlayerRedeemTable.player eq player and (PlayerRedeemTable.code eq codeKey)
                }.empty()
                if (alreadyUsed) {
                    return@trans blora.redeem.RedeemResult.AlreadyUsedByPlayer
                }

                if (code.oneUse) {
                    val rows = RedeemTable.update({
                        (RedeemTable.id eq code.id) and
                            (RedeemTable.oneUse eq true) and
                            (RedeemTable.used eq false)
                    }) {
                        it[used] = true
                    }
                    if (rows != 1) {
                        return@trans blora.redeem.RedeemResult.OneUseAlreadyConsumed
                    }
                }

                try {
                    PlayerRedeemDao.new {
                        this.player = player
                        this.code = codeKey
                    }
                } catch (_: Exception) {
                    return@trans blora.redeem.RedeemResult.AlreadyUsedByPlayer
                }

                val attachmentSnapshot = code.parseAttachment.clone()
                val mail = MailDao.new {
                    this.receiver = player
                    this.parsedSender = Sender.System(mailSenderName)
                    this.title = mailTitle
                    this.contents = mailContents
                    this.parsedAttachment = attachmentSnapshot
                    this.creator = code.creator
                    this.createdAt = LocalDateTime.now()
                    this.systemMailId = null
                    this.visible = true
                    this.isRead = false
                    this.isClaim = attachmentSnapshot.hasNoContent()
                }
                blora.redeem.RedeemResult.Success(mail)
            }
        } catch (ex: Exception) {
            BloraPlugin.slF4JLogger.error("redeemCode failed for $player code=$codeKey", ex)
            blora.redeem.RedeemResult.Failed
        }
    }

    fun isRedeemOneUseAndUsed(redeem: String): Boolean {
        return trans {
            val result = RedeemDao.find {
                RedeemTable.code eq redeem.lowercase()
            }
            if (result.count() > 0) {
                val first = result.first()
                first.oneUse && first.used
            } else {
                false
            }
        }
    }

    fun isRedeemCodeUsedForPlayer(player: UUID, code: String): Boolean {
        return trans {
            !PlayerRedeemDao.find {
                PlayerRedeemTable.player eq player and
                        (PlayerRedeemTable.code eq code.lowercase())
            }.empty()
        }
    }

    fun isRedeemCodeExists(code: String): Boolean {
        return trans {
            RedeemDao.find {
                RedeemTable.code eq code.lowercase()
            }.count() > 0
        }
    }

    /**
     * Single-statement claim acquisition.
     * @return true if this caller won the claim (rows updated == 1)
     */
    fun tryBeginMailClaim(mailId: Int, receiver: UUID): Boolean {
        return trans {
            val rows = MailTable.update({
                (MailTable.id eq mailId) and
                    (MailTable.receiver eq receiver) and
                    (MailTable.isClaim eq false)
            }) {
                it[isClaim] = true
            }
            rows == 1
        }
    }

    /**
     * Takes a transaction-scoped PostgreSQL advisory lock for a reward operation.
     * Hash collisions can only serialize unrelated rewards; they cannot bypass the lock.
     */
    private fun Transaction.tryAcquireRewardLock(player: UUID, type: String, rewardId: String): Boolean {
        val playerKey = player.hashCode()
        val rewardKey = "$type:$rewardId".hashCode()
        return exec("SELECT pg_try_advisory_xact_lock($playerKey, $rewardKey)") { result ->
            result.next() && result.getBoolean(1)
        } == true
    }

    // System Mail

    fun deleteSystemMailById(id: String) {
        val systemMail = getSystemMailByIdentifier(id)
        if (systemMail != null) {
            trans {
                systemMail.delete()
            }
        }
        trans {
            MailDao.find {
                MailTable.systemMailId eq id
            }.forEach {
                it.delete()
                it.flush()
            }
        }
    }

    fun getSystemMailByIdentifier(id: String): SystemMailDao? {
        return trans {
            SystemMailDao.find {
                SystemMailTable.identifier eq id
            }.firstOrNull()
        }
    }

    fun isPlayerReceivedSystemMail(player: UUID, systemMailId: String): Boolean {
        return trans {
            MailDao.find {
                MailTable.receiver eq player and
                        (MailTable.systemMailId eq systemMailId)
            }.count() > 0
        }
    }

    /**
     * Atomically checks and creates one system mail for a player without requiring a unique index.
     * Lock contention or any database failure is fail-closed and creates no mail.
     */
    fun tryReceiveSystemMail(player: UUID, systemMailId: String, visible: Boolean): MailDao? {
        return try {
            trans {
                if (!tryAcquireRewardLock(player, "system-mail", systemMailId)) {
                    BloraPlugin.slF4JLogger.warn(
                        "[SECURITY] Concurrent system mail delivery rejected for $player mail=$systemMailId"
                    )
                    return@trans null
                }

                val alreadyReceived = !MailDao.find {
                    (MailTable.receiver eq player) and
                        (MailTable.systemMailId eq systemMailId)
                }.empty()
                if (alreadyReceived) {
                    return@trans null
                }

                val systemMail = SystemMailDao.find {
                    SystemMailTable.identifier eq systemMailId
                }.firstOrNull() ?: return@trans null
                val attachmentSnapshot = systemMail.parsedAttachment.clone()

                MailDao.new {
                    this.receiver = player
                    this.parsedSender = Sender.System(systemMail.sender ?: "")
                    this.title = systemMail.title
                    this.contents = systemMail.contents
                    this.parsedAttachment = attachmentSnapshot
                    this.creator = systemMail.creator
                    this.createdAt = systemMail.sendingDate ?: LocalDateTime.now()
                    this.systemMailId = systemMail.identifier
                    this.visible = visible
                    this.isRead = false
                    // Invisible = never-acquirable placeholder (trigger fail path).
                    // Mark claimed so we never leave unclaimable rewards sitting in DB.
                    this.isClaim = !visible || attachmentSnapshot.hasNoContent()
                }
            }
        } catch (ex: Exception) {
            BloraPlugin.slF4JLogger.error(
                "tryReceiveSystemMail failed for $player mail=$systemMailId",
                ex
            )
            null
        }
    }

    fun listSystemMails(): List<SystemMailDao> {
        return trans { SystemMailDao.all().toList() }
    }

    fun isSystemMailIdUsed(id: String): Boolean {
        return trans {
            SystemMailDao.find {
                SystemMailTable.identifier eq id
            }.count() > 0
        }
    }

    fun saveSystemMailWithContext(creator: UUID, id: String, context: CreatingMailContext) {
        trans {
            SystemMailDao.new {
                this.identifier = id
                this.sender = context.sender.ifEmpty { null }
                this.title = context.title
                this.contents = context.contents
                this.parsedTriggers = context.triggers
                this.parsedAttachment = context.attachment
                this.creator = creator
                this.sendingDate = context.date
                this.createdAt = LocalDateTime.now()
            }
        }
    }

    // Mail

    fun getMailsByReceiverUuid(uuid: UUID): List<MailDao> {
        return trans {
            MailDao.find {
                MailTable.receiver eq uuid
            }.toList()
        }
    }

    fun getMailsBySender(sender: Sender): List<MailDao> {
        return trans {
            MailDao.find {
                MailTable.sender eq sender.serialize()
            }.toList()
        }
    }

    fun getMailById(id: Int): MailDao? {
        return trans {
            MailDao.findById(id)
        }
    }

    // Player

    fun getPlayerOnlineData(player: UUID): PlayerOnlineData {
        return trans {
            PlayerOnlineDataDao.find {
                PlayerOnlineDataTable.uuid eq player
            }.firstOrNull()?.onlineData ?: PlayerOnlineData(emptyList())
        }
    }

    fun getPlayerDisplayName(uuid: UUID): String {
        return trans {
            val onlinePlayer = Bukkit.getPlayer(uuid)
            if (onlinePlayer != null && onlinePlayer.isOnline) {
                return@trans onlinePlayer.name
            }

            val infoResult = PlayerInfoDao.find {
                PlayerInfoTable.uuid eq uuid
            }.firstOrNull()
            if (infoResult != null) {
                return@trans infoResult.username // case preserved
            }

            val result = PlayerDao.find {
                PlayerTable.uuid eq uuid
            }
            if (result.count() == 0L) {
                return@trans uuid.toString()
            }
            val playerDao = result.toList()[0]
            val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
            if (offlinePlayer.name == null) {
                return@trans playerDao.username // case not preserved
            }
            if (offlinePlayer.name!!.lowercase() == playerDao.username) {
                return@trans offlinePlayer.name!! // case preserved
            }
            return@trans playerDao.username // case not preserved
        }
    }

    fun disconnect() {
        this.dataSource.close()
    }

}
