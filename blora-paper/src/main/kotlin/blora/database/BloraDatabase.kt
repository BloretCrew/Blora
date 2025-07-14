package blora.database

import blora.database.mail.MailDao
import blora.database.mail.MailTable
import blora.database.mail.SystemMailDao
import blora.database.mail.SystemMailTable
import blora.database.player.PlayerDao
import blora.database.player.PlayerLoginDao
import blora.database.player.PlayerLoginTable
import blora.database.player.PlayerTable
import blora.database.redeem.PlayerRedeemDao
import blora.database.redeem.PlayerRedeemTable
import blora.database.redeem.RedeemDao
import blora.database.redeem.RedeemTable
import blora.modules.mail.CreatingMailContext
import blora.modules.mail.Sender
import blora.player.PlayerLoginData
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class BloraDatabase(
    private val dataSource: HikariDataSource,
) {

    val database = Database.connect(dataSource)

    fun <T> trans(statement: Transaction.() -> T): T {
        return transaction(database, statement)
    }

    fun initTables() {
        trans {
            SchemaUtils.create(PlayerTable)
            SchemaUtils.create(PlayerLoginTable)
            SchemaUtils.create(MailTable)
            SchemaUtils.create(SystemMailTable)
            SchemaUtils.create(RedeemTable)
            SchemaUtils.create(PlayerRedeemTable)
        }
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

    fun redeemCode(player: UUID, redeem: String) {
        trans {
            val result = RedeemDao.find {
                RedeemTable.code eq redeem.lowercase()
            }
            val code = result.firstOrNull()
            if (code == null)
                return@trans
            if (code.oneUse && code.used)
                return@trans
            if (code.oneUse) {
                code.used = true
                code.flush()
            }
            PlayerRedeemDao.new {
                this.player = player
                this.code = redeem.lowercase()
            }
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
            PlayerRedeemDao.find {
                PlayerRedeemTable.player eq player
                PlayerRedeemTable.code eq code.lowercase()
            }.count() > 0
        }
    }

    fun isRedeemCodeExists(code: String): Boolean {
        return trans {
            RedeemDao.find {
                RedeemTable.code eq code.lowercase()
            }.count() > 0
        }
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
                MailTable.receiver eq player
                MailTable.systemMailId eq systemMailId
            }.count() > 0
        }
    }

    fun getCreatorDisplayName(mail: SystemMailDao): String {
        return trans {
            val result = PlayerDao.find {
                PlayerTable.uuid eq mail.creator
            }
            if (result.count() > 0) {
                result.first().username
            } else {
                mail.creator.toString()
            }
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

    fun getPlayerDisplayName(uuid: UUID): String {
        return trans {
            val result = PlayerDao.find {
                PlayerTable.uuid eq uuid
            }
            if (result.count() == 0L) {
                return@trans uuid.toString()
            }
            return@trans result.toList()[0].username
        }
    }

    fun getPlayerByName(name: String): PlayerDao? {
        return trans {
            val result = PlayerDao.find {
                PlayerTable.username eq name.lowercase()
            }
            if (result.count() == 0L) {
                return@trans null
            }
            return@trans result.toList()[0]
        }
    }

    fun getPlayerByUuid(uuid: UUID): PlayerDao? {
        return trans {
            val result = PlayerDao.find {
                PlayerTable.uuid eq uuid
            }
            if (result.count() == 0L) {
                return@trans null
            }
            return@trans result.toList()[0]
        }
    }

    fun getPlayerByPremiumUuid(uuid: UUID): PlayerDao? {
        return trans {
            val result = PlayerDao.find {
                PlayerTable.premiumUuid eq uuid
            }
            if (result.count() == 0L) {
                return@trans null
            }
            return@trans result.toList()[0]
        }
    }

    fun getFirstIpAmount(ip: String): Int {
        return trans {
            PlayerDao.find {
                PlayerTable.firstIp eq ip
            }.count()
                .toInt()
        }
    }

    fun getLastIpAmount(ip: String): Int {
        return trans {
            PlayerDao.find {
                PlayerTable.lastIp eq ip
            }.count()
                .toInt()
        }
    }

    fun disconnect() {
        this.dataSource.close()
    }

}