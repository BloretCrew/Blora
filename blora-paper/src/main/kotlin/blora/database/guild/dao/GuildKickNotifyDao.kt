package blora.database.guild.dao

import blora.database.guild.table.GuildBlocklistTable
import blora.database.guild.table.GuildKickNotifyTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildKickNotifyDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildKickNotifyDao>(GuildKickNotifyTable)

    var guildId by GuildKickNotifyTable.gid
    var guildName by GuildKickNotifyTable.name
    var player by GuildKickNotifyTable.player
    var operator by GuildKickNotifyTable.operator
    var kickAt by GuildKickNotifyTable.kickAt

}