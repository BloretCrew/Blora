package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable

object GuildPlayerBalanceTable : IntIdTable("blora_guild_player_balance") {

    val player = uuid("player")
    val gid = text("gid")
    val contribution = double("contribution")

}