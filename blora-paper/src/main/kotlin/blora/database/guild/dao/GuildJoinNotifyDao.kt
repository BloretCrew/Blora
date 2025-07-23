package blora.database.guild.dao

import blora.database.guild.table.GuildJoinNotifyTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildJoinNotifyDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildJoinNotifyDao>(GuildJoinNotifyTable)

    var guildId: String by GuildJoinNotifyTable.gid
    var guildName: String by GuildJoinNotifyTable.name
    var player by GuildJoinNotifyTable.player
    var result by GuildJoinNotifyTable.result

}