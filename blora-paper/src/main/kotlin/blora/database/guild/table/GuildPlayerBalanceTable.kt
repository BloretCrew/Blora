package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable

object GuildPlayerBalanceTable : IntIdTable("blora_guild_player_balance") {

    val gid = text("gid")
    val player = uuid("player")
    val contribution = double("contribution")

}