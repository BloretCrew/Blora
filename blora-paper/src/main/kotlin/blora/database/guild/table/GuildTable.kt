package blora.database.guild.table

import blora.guild.GuildEnderChest
import blora.guild.GuildJoinStrategy
import blora.json.MINECRAFT_DATA_JSON
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.json.json

object GuildTable : IntIdTable("blora_guilds") {

    val gid = text("guild_id") // letters and digits only
    val owner = uuid("owner")
    val public = bool("public").default(true)
    val icon = text("icon")
    val displayName = text("display_name")
    val level = integer("level").default(1)

    val joinStrategy = enumeration<GuildJoinStrategy>("join_strategy").default(GuildJoinStrategy.REQUIRE_REVIEW)

    val members = array("members", UUIDColumnType())
    val towns = array("towns", TextColumnType())
    val allys = array("allys", TextColumnType())
    val blocklist = array("blocklist", UUIDColumnType())

    val bankBalance = double("bank_balance").default(0.0)
    val bankBalanceMax = double("bank_balance_max").default(0.0)

    val vitality = double("vitality").default(0.0)

    val createdAt = datetime("created_at")
    val lastOwnerTransferDate = date("last_owner_transfer_date")

    val enderChest = json<GuildEnderChest>("ender_chest", MINECRAFT_DATA_JSON).default(GuildEnderChest())

}