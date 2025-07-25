package blora.database.guild.dao

import blora.database.guild.table.GuildPlayerBalanceTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildPlayerBalanceDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildPlayerBalanceDao>(GuildPlayerBalanceTable)

    var guildId by GuildPlayerBalanceTable.gid
    var player by GuildPlayerBalanceTable.player
    var contribution by GuildPlayerBalanceTable.contribution

}