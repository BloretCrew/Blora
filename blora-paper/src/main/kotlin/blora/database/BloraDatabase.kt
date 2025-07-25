package blora.database

import blora.configuration.CONF
import blora.database.guild.dao.*
import blora.database.guild.table.*
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
import blora.extension.format
import blora.extension.localization
import blora.formula.FormulaTokenizer
import blora.guild.GuildJoinSource
import blora.guild.ReviewResult
import blora.guild.formula.GuildFormulaVariable
import blora.guild.role.RolePermissions
import blora.guild.role.merge
import blora.mail.CreatingMailContext
import blora.mail.Sender
import blora.player.PapiFormulaVariable
import blora.player.PlayerLoginData
import blora.player.PlayerOnlineData
import blora.plugin.BloraPlugin
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDateTime
import java.util.*

val DB: BloraDatabase
    get() = BloraPlugin.database

class BloraDatabase(
    private val dataSource: HikariDataSource,
) {

    val database = Database.connect(dataSource)

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
            // Guild
            SchemaUtils.create(GuildTable)
            SchemaUtils.create(GuildRoleTable)
            SchemaUtils.create(GuildInviteCodeTable)
            SchemaUtils.create(GuildInvitationTable)
            SchemaUtils.create(GuildPlayerBalanceTable)
            SchemaUtils.create(GuildJoinNotifyTable)
            SchemaUtils.create(GuildDisbandNotifyTable)
            SchemaUtils.create(GuildMemberInfoTable)
            SchemaUtils.create(GuildJoinRequestTable)
            SchemaUtils.create(GuildBankLogTable)
            SchemaUtils.create(GuildBlocklistTable)
            SchemaUtils.create(GuildKickNotifyTable)
            SchemaUtils.create(GuildPlayerOnlineTable)
            SchemaUtils.create(GuildJoinLogTable)
            SchemaUtils.create(GuildBloriusToVitalityTimesTable)
        }
    }

    // Guild

    fun getBloriusToVitalityTimes(guild: String): GuildBloriusToVitalityTimesDao? {
        return trans {
            GuildBloriusToVitalityTimesDao.find {
                GuildBloriusToVitalityTimesTable.gid eq guild
            }.firstOrNull()
        }
    }

    fun getGuildDisplayName(guildId: String): String? {
        return getGuildByGid(guildId)?.displayName
    }

    fun listPlayerOnlines(player: UUID): List<GuildPlayerOnlineDao> {
        return trans {
            GuildPlayerOnlineDao.find {
                GuildPlayerOnlineTable.player eq player
            }.toList()
        }
    }

    fun listJoinNotifies(player: UUID): List<GuildJoinNotifyDao> {
        return trans {
            GuildJoinNotifyDao.find {
                GuildJoinNotifyTable.player eq player
            }.toList()
        }
    }

    fun listDisbandNotifies(player: UUID): List<GuildDisbandNotifyDao> {
        return trans {
            GuildDisbandNotifyDao.find {
                QueryParameter(player, UUIDColumnType()) eq anyFrom(GuildDisbandNotifyTable.members)
            }.toList()
        }
    }

    fun listKickNotifies(player: UUID): List<GuildKickNotifyDao> {
        return trans {
            GuildKickNotifyDao.find {
                GuildKickNotifyTable.player eq player
            }.toList()
        }
    }

    fun listBlocklist(guild: String): List<GuildBlocklistDao> {
        return trans {
            GuildBlocklistDao.find {
                GuildBlocklistTable.gid eq guild
            }.toList()
        }
    }

    fun getInvitationCode(code: String): GuildInviteCodeDao? {
        return trans {
            GuildInviteCodeDao.find {
                GuildInviteCodeTable.inviteCode eq code
            }.firstOrNull()
        }
    }

    fun listInvitationCodes(guild: String): List<GuildInviteCodeDao> {
        return trans {
            GuildInviteCodeDao.find {
                GuildInviteCodeTable.gid eq guild
            }.toList()
        }
    }

    fun isInvited(player: UUID, guild: String): Boolean {
        return trans {
            !GuildInvitationDao.find {
                GuildInvitationTable.invitee eq player and
                        (GuildInvitationTable.gid eq guild)
            }.empty()
        }
    }

    fun listGuildInvites(player: UUID): List<GuildInvitationDao> {
        return trans {
            GuildInvitationDao.find {
                GuildInvitationTable.invitee eq player
            }.toList()
        }
    }

    fun listValidJoinRequestsForGuild(guild: String): List<GuildJoinRequestDao> {
        return trans {
            GuildJoinRequestDao.find {
                GuildJoinRequestTable.gid eq guild and
                        (GuildJoinRequestTable.finished eq false)
            }.toList()
        }
    }

    fun getRolePermissions(player: UUID, guild: String): RolePermissions {
        val guildDao = getGuildByGid(guild)
        if (guildDao == null || !guildDao.members.contains(player))
            return RolePermissions.denyAll()
        if (guildDao.owner == player)
            return RolePermissions.allowAll()
        val roles = listRolesForPlayer(player, guild)
        return roles.map { it.permission }.merge()
    }

    fun getMaxRolePriority(player: UUID, guild: GuildDao): Int {
        if (guild.owner == player)
            return 4
        return listRolesForPlayer(player, guild.gid).maxOf { it.priority }
    }

    fun listRolePriorities(guild: GuildDao): Map<UUID, Int> {
        val map = mutableMapOf<UUID, Int>()
        for (role in listRolesForGuild(guild.gid)) {
            for (member in role.ownedMembers) {
                if (member == guild.owner) {
                    map[member] = 4
                    continue
                }
                val stored = map[member]
                if (stored != null && stored >= role.priority)
                    continue
                map[member] = role.priority
            }
        }
        return map.toMap()
    }

    fun listRolePermissions(players: Collection<UUID>, guild: String): Map<UUID, RolePermissions> {
        val roles = listRolesForGuild(guild)
        return players.map { player ->
            player to roles.toList()
                .filter { it.ownedMembers.contains(player) }
                .map { it.permission }
                .merge()
        }.associate { it }
    }

    fun getValidJoinRequest(guild: String, player: UUID): GuildJoinRequestDao? {
        return trans {
            GuildJoinRequestDao.find {
                GuildJoinRequestTable.gid eq guild and
                        (GuildJoinRequestTable.player eq player) and
                        (GuildJoinRequestTable.finished eq false)
            }.firstOrNull()
        }
    }

    fun announceJoinRequest(guild: GuildDao, player: Player) {
        listRolePermissions(guild.members, guild.gid)
            .filter { it.value.reviewPlayer }
            .keys
            .mapNotNull { Bukkit.getPlayer(it) }
            .filter { it.isOnline }
            .forEach {
                it.send {
                    localization(
                        it,
                        tags = {
                            parsedPlaceholder("player", player.name)
                            parsedPlaceholder("guild", guild.displayName)
                        }
                    ) {
                        this.guild.guildJoin_requestAnnouncement
                    }
                }
            }
    }

    fun createJoinRequest(guild: GuildDao, player: Player, source: GuildJoinSource) {
        trans {
            GuildJoinRequestDao.new {
                this.guildId = guild.gid
                this.player = player.uniqueId
                this.requestAt = LocalDateTime.now()
                this.parsedJoinSource = source
            }
        }
    }

    fun announceJoin(guild: GuildDao, player: Player) {
        guild.members.mapNotNull { Bukkit.getPlayer(it) }
            .filter { it.isOnline }
            .filter { it != player } // excluded himself
            .forEach {
                it.send {
                    localization(
                        it,
                        tags = {
                            parsedPlaceholder("player", player.name)
                            parsedPlaceholder("guild", guild.displayName)
                        }
                    ) {
                        this.guild.guildJoinAnnouncement
                    }
                }
            }
    }

    fun guildJoinDirect(guild: GuildDao, player: UUID, joinSource: GuildJoinSource) {
        trans {
            val joinLog = GuildJoinLogDao.find {
                GuildJoinLogTable.gid eq guild.gid and
                        (GuildJoinLogTable.player eq player)
            }
            if (joinLog.empty()) {
                GuildJoinLogDao.new {
                    this.guildId = guild.gid
                    this.player = player
                    this.firstJoin = LocalDateTime.now()
                }
                val formula = FormulaTokenizer.parse(CONF.guild.vitality.newPlayerJoinVitalityFormula)
                if (formula == null) {
                    BloraPlugin.slF4JLogger.error("新玩家加入计算公式无法正常解析(1)，请调整后重启服务器")
                } else {
                    val guildVariable = GuildFormulaVariable(guild)
                    val papiVariable = PapiFormulaVariable(Bukkit.getOfflinePlayer(player))
                    val value = formula.calculate(guildVariable, papiVariable)
                    if (value == null) {
                        BloraPlugin.slF4JLogger.error("新玩家加入计算公式无法正常解析(2)，请调整后重启服务器")
                    } else {
                        guild.vitality += value
                        guild.flush()
                        Bukkit.getOnlinePlayers()
                            .filter { player -> guild.members.contains(player.uniqueId) }
                            .forEach { onlinePlayer ->
                                onlinePlayer.send {
                                    localization(
                                        player = onlinePlayer,
                                        tags = {
                                            parsedPlaceholder("guild", guild.displayName)
                                            parsedPlaceholder("vitality", value.format(2))
                                            componentPlaceholder("reason") {
                                                localization(
                                                    player = onlinePlayer,
                                                    tags = {
                                                        parsedPlaceholder("player", DB.getPlayerDisplayName(player))
                                                    }
                                                ) {
                                                    this.guild.guildVitalityAddReasonNew_player_join
                                                }
                                            }
                                        }
                                    ) {
                                        this.guild.guildVitalityAdd
                                    }
                                }
                            }
                    }
                }
            }
            guild.members = guild.members.toMutableList().apply { this.add(player) }.toList()
            guild.flush()
            GuildInvitationTable.deleteWhere {
                GuildInvitationTable.invitee eq player
            }
            GuildPlayerOnlineDao.new {
                this.guildId = guild.gid
                this.player = player
                this.lastCalculate = LocalDateTime.now()
            }
            GuildMemberInfoDao.new {
                this.guildId = guild.gid
                this.player = player
                this.joinAt = LocalDateTime.now()

                this.parsedJoinSource = joinSource
            }
            val memberRole = getMemberRole(guild)
            memberRole.ownedMembers = memberRole.ownedMembers.toMutableList().apply { this.add(player) }.toList()
            memberRole.flush()
        }
    }

    fun guildJoinInRequest(
        guild: GuildDao,
        player: UUID,
        joinSource: GuildJoinSource,
        joinRequest: GuildJoinRequestDao,
        reviewResult: ReviewResult
    ) {
        trans {
            val joinLog = GuildJoinLogDao.find {
                GuildJoinLogTable.gid eq guild.gid and
                        (GuildJoinLogTable.player eq player)
            }
            if (joinLog.empty()) {
                GuildJoinLogDao.new {
                    this.guildId = guild.gid
                    this.player = player
                    this.firstJoin = LocalDateTime.now()
                }
                // TODO: add vitality
            }
            guild.members = guild.members.toMutableList().apply { this.add(player) }.toList()
            guild.flush()
            GuildInvitationTable.deleteWhere {
                GuildInvitationTable.invitee eq player
            }
            GuildPlayerOnlineDao.new {
                this.guildId = guild.gid
                this.player = player
                this.lastCalculate = LocalDateTime.now()
            }
            GuildMemberInfoDao.new {
                this.guildId = guild.gid
                this.player = player
                this.joinAt = joinRequest.requestAt

                this.reviewer = reviewResult.reviewer
                this.reviewedAt = reviewResult.reviewedAt

                this.parsedJoinSource = joinSource
            }
            val memberRole = getMemberRole(guild)
            memberRole.ownedMembers = memberRole.ownedMembers.toMutableList().apply { this.add(player) }.toList()
            memberRole.flush()
        }
    }

    fun listAllys(guild: GuildDao): List<GuildDao> {
        return guild.allys.mapNotNull { getGuildByGid(it) }.toList()
    }

    fun disbandGuild(guild: GuildDao) {
        trans {
            GuildBankLogTable.deleteWhere {
                GuildBankLogTable.gid eq guild.gid
            }
            GuildBlocklistTable.deleteWhere {
                GuildBlocklistTable.gid eq guild.gid
            }
            GuildInvitationTable.deleteWhere {
                GuildInvitationTable.gid eq guild.gid
            }
            GuildInviteCodeTable.deleteWhere {
                GuildInviteCodeTable.gid eq guild.gid
            }
            GuildJoinLogTable.deleteWhere {
                GuildJoinLogTable.gid eq guild.gid
            }
            GuildJoinNotifyTable.deleteWhere {
                GuildJoinNotifyTable.gid eq guild.gid
            }
            GuildJoinRequestTable.deleteWhere {
                GuildJoinRequestTable.gid eq guild.gid
            }
            GuildKickNotifyTable.deleteWhere {
                GuildKickNotifyTable.gid eq guild.gid
            }
            GuildMemberInfoTable.deleteWhere {
                GuildMemberInfoTable.gid eq guild.gid
            }
            GuildPlayerBalanceTable.deleteWhere {
                GuildPlayerBalanceTable.gid eq guild.gid
            }
            GuildPlayerOnlineTable.deleteWhere {
                GuildPlayerOnlineTable.gid eq guild.gid
            }
            GuildRoleTable.deleteWhere {
                GuildRoleTable.gid eq guild.gid
            }
            for (ally in listAllys(guild)) {
                ally.allys = ally.allys.toMutableList().apply { this.remove(guild.gid) }.toList()
                ally.flush()
            }
            GuildDisbandNotifyDao.new {
                this.guildId = guild.gid
                this.guildName = guild.displayName
                this.members = guild.members.filter {
                    val player = Bukkit.getPlayer(it)
                    if (player == null)
                        return@filter true
                    if (!player.isOnline)
                        return@filter true
                    player.send {
                        localization(
                            player = player,
                            tags = {
                                parsedPlaceholder("guild_id", guild.gid)
                                parsedPlaceholder("guild_name", guild.displayName)
                            }
                        ) {
                            this.guild.guildDisbandNotify
                        }
                    }
                    return@filter false
                }
            }
            guild.delete()
        }
    }

    fun updateGuildId(guild: GuildDao, newId: String) {
        trans {
            // TODO: update town gids
            GuildBankLogTable.update({
                GuildBankLogTable.gid eq guild.gid
            }) {
                it.update(GuildBankLogTable.gid, stringParam(newId))
            }
            GuildBlocklistTable.update({
                GuildBlocklistTable.gid eq guild.gid
            }) {
                it.update(GuildBlocklistTable.gid, stringParam(newId))
            }
            GuildInvitationTable.update({
                GuildInvitationTable.gid eq guild.gid
            }) {
                it.update(GuildInvitationTable.gid, stringParam(newId))
            }
            GuildInviteCodeTable.update({
                GuildInviteCodeTable.gid eq guild.gid
            }) {
                it.update(GuildInviteCodeTable.gid, stringParam(newId))
            }
            GuildJoinLogTable.update({
                GuildJoinLogTable.gid eq guild.gid
            }) {
                it.update(GuildJoinLogTable.gid, stringParam(newId))
            }
            GuildJoinNotifyTable.update({
                GuildJoinNotifyTable.gid eq guild.gid
            }) {
                it.update(GuildJoinNotifyTable.gid, stringParam(newId))
            }
            GuildJoinRequestTable.update({
                GuildJoinRequestTable.gid eq guild.gid
            }) {
                it.update(GuildJoinRequestTable.gid, stringParam(newId))
            }
            GuildKickNotifyTable.update({
                GuildKickNotifyTable.gid eq guild.gid
            }) {
                it.update(GuildKickNotifyTable.gid, stringParam(newId))
            }
            GuildMemberInfoTable.update({
                GuildMemberInfoTable.gid eq guild.gid
            }) {
                it.update(GuildMemberInfoTable.gid, stringParam(newId))
            }
            GuildPlayerBalanceTable.update({
                GuildPlayerBalanceTable.gid eq guild.gid
            }) {
                it.update(GuildPlayerBalanceTable.gid, stringParam(newId))
            }
            GuildPlayerOnlineTable.update({
                GuildPlayerOnlineTable.gid eq guild.gid
            }) {
                it.update(GuildPlayerOnlineTable.gid, stringParam(newId))
            }
            GuildRoleTable.update({
                GuildRoleTable.gid eq guild.gid
            }) {
                it.update(GuildRoleTable.gid, stringParam(newId))
            }
            guild.gid = newId
            guild.flush()
        }
    }

    fun getAdminRole(guild: GuildDao): GuildRoleDao {
        return trans {
            GuildRoleDao.find {
                GuildRoleTable.gid eq guild.gid and
                        (GuildRoleTable.rid eq "admin")
            }.first()
        }
    }

    fun getMemberRole(guild: GuildDao): GuildRoleDao {
        return trans {
            GuildRoleDao.find {
                GuildRoleTable.gid eq guild.gid and
                        (GuildRoleTable.rid eq "member")
            }.first()
        }
    }

    fun listMemberInfo(guild: String): List<GuildMemberInfoDao> {
        return trans {
            GuildMemberInfoDao.find {
                GuildMemberInfoTable.gid eq guild
            }.toList()
        }
    }

    fun getMemberInfo(guild: String, player: UUID): GuildMemberInfoDao? {
        return trans {
            GuildMemberInfoDao.find {
                GuildMemberInfoTable.gid eq guild and
                        (GuildMemberInfoTable.player eq player)
            }.firstOrNull()
        }
    }

    fun getRoleForGuild(guild: String, roleId: String): GuildRoleDao? {
        return trans {
            GuildRoleDao.find {
                GuildRoleTable.gid eq guild and
                        (GuildRoleTable.rid eq roleId)
            }.firstOrNull()
        }
    }

    fun listRolesForPlayer(player: UUID, guild: String): List<GuildRoleDao> {
        return trans {
            GuildRoleDao.find {
                QueryParameter(player, UUIDColumnType()) eq anyFrom(GuildRoleTable.ownedMembers) and
                        (GuildRoleTable.gid eq guild)
            }
                .toList()
                .sortedWith { first, second ->
                    val priorityCompare = first.priority.compareTo(second.priority)
                    if (priorityCompare != 0)
                        return@sortedWith -priorityCompare
                    return@sortedWith first.roleId.compareTo(second.roleId)
                }
        }
    }

    fun listRolesForGuild(guild: String): List<GuildRoleDao> {
        return trans {
            GuildRoleDao.find {
                GuildRoleTable.gid eq guild
            }
                .toList()
                .sortedWith { first, second ->
                    val priorityCompare = first.priority.compareTo(second.priority)
                    if (priorityCompare != 0)
                        return@sortedWith priorityCompare
                    return@sortedWith first.roleId.compareTo(second.roleId)
                }
        }
    }

    fun getGuildByGid(gid: String): GuildDao? {
        return trans {
            GuildDao.find {
                GuildTable.gid eq gid
            }.firstOrNull()
        }
    }

    fun listGuilds(): List<GuildDao> {
        return trans {
            GuildDao.all().toList()
        }
    }

    fun listGuildForPlayer(player: UUID): List<GuildDao> {
        return trans {
            GuildDao.find {
                QueryParameter(player, UUIDColumnType()) eq anyFrom(GuildTable.members)
            }.toList()
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