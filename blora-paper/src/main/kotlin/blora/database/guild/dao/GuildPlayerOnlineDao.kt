package blora.database.guild.dao

import blora.database.guild.table.GuildJoinLogTable
import blora.database.guild.table.GuildPlayerOnlineTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildPlayerOnlineDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildPlayerOnlineDao>(GuildPlayerOnlineTable)

    var guildId by GuildPlayerOnlineTable.gid
    var player by GuildPlayerOnlineTable.player
    var lastCalculate by GuildPlayerOnlineTable.lastCalculate

}