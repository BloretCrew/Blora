package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildAllyRequestTable : IntIdTable("blora_guild_ally_request") {

    val requestGid = text("request_gid")
    val receiveGid = text("receive_gid")

    val requestOperator = uuid("request_operator")
    val requestOperatedAt = datetime("request_operated_at")

    val finished = bool("finished").default(false)
    val result = bool("result").nullable().default(null)
    val receiveOperator = uuid("receive_operator").nullable().default(null)
    val receiveOperatedAt = datetime("receive_operated_at").nullable().default(null)

}