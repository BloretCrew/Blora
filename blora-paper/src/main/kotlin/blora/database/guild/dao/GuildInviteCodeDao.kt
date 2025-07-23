package blora.database.guild.dao

import blora.database.guild.table.GuildInviteCodeTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildInviteCodeDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildInviteCodeDao>(GuildInviteCodeTable)

    var guildId by GuildInviteCodeTable.gid
    var inviteCode by GuildInviteCodeTable.inviteCode
    var singleUsable by GuildInviteCodeTable.singleUsable
    var expireAt by GuildInviteCodeTable.expireAt

    var creator by GuildInviteCodeTable.creator
    var createdAt by GuildInviteCodeTable.createdAt

    var outdated by GuildInviteCodeTable.outdated

}