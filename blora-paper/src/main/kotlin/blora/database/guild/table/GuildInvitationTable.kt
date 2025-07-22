package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildInvitationTable : IntIdTable("blora_guild_invitation") {

    val gid = text("guild_id")
    val invitee = uuid("invitee")
    val inviter = uuid("inviter")
    val invitedAt = datetime("invited_at")

}