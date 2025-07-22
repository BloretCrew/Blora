package blora.database.guild.dao

import blora.database.guild.table.GuildDisbandNotifyTable
import blora.database.guild.table.GuildTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildDisbandNotifyDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildDisbandNotifyDao>(GuildDisbandNotifyTable)

    var guildId: String by GuildDisbandNotifyTable.gid
    var guildName: String by GuildDisbandNotifyTable.name
    var members by GuildDisbandNotifyTable.members

}