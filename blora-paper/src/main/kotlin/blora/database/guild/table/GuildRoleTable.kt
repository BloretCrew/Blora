package blora.database.guild.table

import blora.guild.role.RolePermissions
import blora.json.STORE_DATA_JSON
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.json.json

object GuildRoleTable : IntIdTable("blora_guild_roles") {

    val rid = text("role_id")
    val displayName = text("display_name")
    val gid = text("guild_id")
    val systemCreated = bool("system_created")
    val creator = uuid("creator").nullable().default(null)
    val priority = integer("priority") // admin: 3, member: 1; user created role is 2
    val permission = json<RolePermissions>("permission", STORE_DATA_JSON)
    val ownedMembers = array("owned_members", UUIDColumnType())

}