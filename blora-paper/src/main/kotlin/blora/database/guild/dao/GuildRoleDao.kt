package blora.database.guild.dao

import blora.database.guild.table.GuildRoleTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildRoleDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildRoleDao>(GuildRoleTable)

    var roleId by GuildRoleTable.rid
    var displayName by GuildRoleTable.displayName
    var guildId by GuildRoleTable.gid
    var systemCreated by GuildRoleTable.systemCreated
    var creator by GuildRoleTable.creator
    var priority by GuildRoleTable.priority
    var permission by GuildRoleTable.permission
    var ownedMembers by GuildRoleTable.ownedMembers

}