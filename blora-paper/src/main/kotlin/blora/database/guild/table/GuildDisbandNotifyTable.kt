package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.UUIDColumnType

object GuildDisbandNotifyTable : IntIdTable("blora_guild_disband_notify") {

    val gid = text("guild_id")
    val name = text("name")
    val members = array("members", UUIDColumnType())

}