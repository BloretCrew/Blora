package blora.database.guild.dao

import blora.database.guild.table.GuildAllyRequestTable
import blora.database.guild.table.GuildBankLogTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildAllyRequestDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildAllyRequestDao>(GuildAllyRequestTable)

    var requestGuildId by GuildAllyRequestTable.requestGid
    var receiveGuildId by GuildAllyRequestTable.receiveGid

    var requestOperator by GuildAllyRequestTable.requestOperator
    var requestOperatedAt by GuildAllyRequestTable.requestOperatedAt

    var finished by GuildAllyRequestTable.finished
    var result by GuildAllyRequestTable.result
    var receiveOperator by GuildAllyRequestTable.receiveOperator
    var receiveOperatedAt by GuildAllyRequestTable.receiveOperatedAt

}