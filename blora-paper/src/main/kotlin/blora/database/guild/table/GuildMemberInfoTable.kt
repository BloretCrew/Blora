package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildMemberInfoTable : IntIdTable("blora_guild_member_info") {

    val gid = text("guild_id")
    val player = uuid("player")
    val joinAt = datetime("join_at")

    // if guild requires review, below two columns are not null
    val reviewedAt = datetime("reviewed_at").nullable().default(null)
    val reviewer = uuid("reviewer").nullable().default(null)

    val joinSource = text("join_source")

}