package blora.database.guild.dao

import blora.database.guild.table.GuildBankLogTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildBankLogDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildBankLogDao>(GuildBankLogTable)

    var guildId by GuildBankLogTable.gid
    var store by GuildBankLogTable.store
    var value by GuildBankLogTable.value
    var timestamp by GuildBankLogTable.timestamp
    var operator by GuildBankLogTable.operator

}