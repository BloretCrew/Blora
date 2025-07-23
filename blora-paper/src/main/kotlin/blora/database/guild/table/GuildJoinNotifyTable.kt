package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable

object GuildJoinNotifyTable : IntIdTable("blora_guild_join_notify") {

    val gid = text("guild_id")
    val name = text("name")
    val player = uuid("player")
    val result = bool("result")

}