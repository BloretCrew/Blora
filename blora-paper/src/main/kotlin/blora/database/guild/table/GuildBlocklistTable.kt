package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildBlocklistTable : IntIdTable("blora_guild_blocklist") {

    val gid = text("guild_id")
    val player = uuid("player")
    val blockedBy = uuid("blocked_by")
    val blockedAt = datetime("blocked_at")

}