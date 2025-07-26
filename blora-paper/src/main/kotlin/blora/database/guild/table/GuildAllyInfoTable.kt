package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildAllyInfoTable : IntIdTable("blora_guild_ally_info") {

    val requestGid = text("request_gid")
    val requestOperator = uuid("request_operator")
    val requestOperatedAt = datetime("request_operated_at")

    val receiveGid = text("receive_gid")
    val receiveOperator = uuid("receive_operator")
    val receiveOperatedAt = datetime("receive_operated_at")

}