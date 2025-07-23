package blora.database.guild.dao

import blora.database.guild.table.GuildJoinRequestTable
import blora.guild.GuildJoinSource
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildJoinRequestDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildJoinRequestDao>(GuildJoinRequestTable)

    var guildId by GuildJoinRequestTable.gid
    var player by GuildJoinRequestTable.player
    var requestAt by GuildJoinRequestTable.requestAt

    var joinSource by GuildJoinRequestTable.joinSource

    var finished by GuildJoinRequestTable.finished
    var result by GuildJoinRequestTable.result
    var operatedAt by GuildJoinRequestTable.operatedAt
    var operator by GuildJoinRequestTable.operator

    var parsedJoinSource: GuildJoinSource
        get() = GuildJoinSource.decodeFromString(this.joinSource)
        set(value) {
            this.joinSource = value.encodeToString()
        }

}