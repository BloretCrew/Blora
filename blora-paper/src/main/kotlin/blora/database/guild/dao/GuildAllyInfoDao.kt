package blora.database.guild.dao

import blora.database.guild.table.GuildAllyInfoTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildAllyInfoDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildAllyInfoDao>(GuildAllyInfoTable)

    var requestGuildId by GuildAllyInfoTable.requestGid
    var requestOperator by GuildAllyInfoTable.requestOperator
    var requestOperatedAt by GuildAllyInfoTable.requestOperatedAt

    var receiveGuildId by GuildAllyInfoTable.receiveGid
    var receiveOperator by GuildAllyInfoTable.receiveOperator
    var receiveOperatedAt by GuildAllyInfoTable.receiveOperatedAt

}