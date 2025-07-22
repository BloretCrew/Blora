package blora.database.guild.dao

import blora.database.guild.table.GuildInvitationTable
import blora.database.guild.table.GuildTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime
import java.util.UUID

class GuildInvitationDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildInvitationDao>(GuildInvitationTable)

    var guildId: String by GuildInvitationTable.gid
    var invitee: UUID by GuildInvitationTable.invitee
    var inviter: UUID by GuildInvitationTable.inviter
    var invitedAt: LocalDateTime by GuildInvitationTable.invitedAt

}