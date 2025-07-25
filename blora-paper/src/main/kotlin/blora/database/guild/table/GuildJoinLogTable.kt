package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildJoinLogTable : IntIdTable("blora_guild_join_log") {

    val gid = text("guild_id")
    val player = uuid("player")
    val firstJoin = datetime("first_join")

}