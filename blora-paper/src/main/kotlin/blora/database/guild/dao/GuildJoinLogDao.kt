package blora.database.guild.dao

import blora.database.guild.table.GuildJoinLogTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildJoinLogDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildJoinLogDao>(GuildJoinLogTable)

    var guildId by GuildJoinLogTable.gid
    var player by GuildJoinLogTable.player
    var firstJoin by GuildJoinLogTable.firstJoin

}