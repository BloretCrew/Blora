package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object GuildBankLogTable : IntIdTable("blora_guild_bank_log") {

    val gid = text("gid")
    val store = bool("store") // false for withdraw
    val value = double("value") // operated coins
    val timestamp = datetime("timestamp")
    val operator = uuid("operator")

}