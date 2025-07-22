package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildJoinRequestTable : IntIdTable("blora_guild_join_requests") {


    val gid = text("guild_id")
    val player = uuid("player")
    val requestAt = datetime("request_at")

    val joinSource = text("join_source")

    val finished = bool("finished").default(false)
    val result = bool("result").nullable().default(null)
    val operatedAt = datetime("operated_at").nullable().default(null)
    val operator = uuid("operator").nullable().default(null)

}