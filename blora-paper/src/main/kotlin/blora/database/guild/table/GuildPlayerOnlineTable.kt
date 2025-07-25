package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildPlayerOnlineTable : IntIdTable("blora_guild_player_online") {

    val gid = text("guild_id")
    val player = uuid("player")
    val lastCalculate = datetime("last_calculate")

}