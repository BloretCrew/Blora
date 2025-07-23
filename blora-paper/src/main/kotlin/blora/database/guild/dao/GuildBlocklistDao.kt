package blora.database.guild.dao

import blora.database.guild.table.GuildBankLogTable
import blora.database.guild.table.GuildBlocklistTable
import blora.database.guild.table.GuildDisbandNotifyTable
import blora.database.guild.table.GuildTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildBlocklistDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildBlocklistDao>(GuildBlocklistTable)

    var guildId by GuildBlocklistTable.gid
    var player by GuildBlocklistTable.player
    var blockedBy by GuildBlocklistTable.blockedBy
    var blockedAt by GuildBlocklistTable.blockedAt

}