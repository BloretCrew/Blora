package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object GuildInviteCodeTable : IntIdTable("blora_guild_invite_codes") {

    val gid = text("guild_id")
    val inviteCode = text("invite_code")
    val singleUsable = bool("single_usable")
    val expireAt = date("expire_at").nullable() // null means never expire

    val creator = uuid("creator")
    val createdAt = datetime("created_at")

    val outdated = bool("outdated").default(false)

}