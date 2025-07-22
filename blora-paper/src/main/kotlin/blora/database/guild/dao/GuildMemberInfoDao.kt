package blora.database.guild.dao

import blora.database.guild.table.GuildMemberInfoTable
import blora.database.guild.table.GuildRoleTable
import blora.guild.GuildJoinSource
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildMemberInfoDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildMemberInfoDao>(GuildMemberInfoTable)

    var guildId by GuildMemberInfoTable.gid
    var player by GuildMemberInfoTable.player
    var joinAt by GuildMemberInfoTable.joinAt

    var reviewedAt by GuildMemberInfoTable.reviewedAt
    var reviewer by GuildMemberInfoTable.reviewer

    var joinSource by GuildMemberInfoTable.joinSource

    var parsedJoinSource: GuildJoinSource
        get() = GuildJoinSource.decodeFromString(this.joinSource)
        set(value) {
            this.joinSource = value.encodeToString()
        }

}