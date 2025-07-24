package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildKickNotifyTable : IntIdTable("blora_guild_kick_notify") {

    val gid = text("guild_id")
    val name = text("guild_name")
    val player = uuid("player")
    val operator = uuid("operator")
    val kickAt = datetime("kickAt")

}