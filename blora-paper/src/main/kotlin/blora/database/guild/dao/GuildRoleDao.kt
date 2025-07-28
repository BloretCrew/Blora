package blora.database.guild.dao

import blora.database.DB
import blora.database.guild.table.GuildRoleTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.anyFrom
import java.util.*

class GuildRoleDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildRoleDao>(GuildRoleTable) {

        fun find(guildId: String, roleId: String): GuildRoleDao? {
            return DB.trans {
                find {
                    GuildRoleTable.gid eq guildId and
                            (GuildRoleTable.rid eq roleId)
                }.firstOrNull()
            }
        }

        fun listByGuildId(guildId: String): List<GuildRoleDao> {
            return DB.trans {
                find {
                    GuildRoleTable.gid eq guildId
                }.toList()
            }
        }

        fun listByGuildIdAndOwnedMember(guildId: String, ownedMember: UUID): List<GuildRoleDao> {
            return DB.trans {
                find {
                    GuildRoleTable.gid eq guildId and
                            (QueryParameter(ownedMember, UUIDColumnType()) eq anyFrom(GuildRoleTable.ownedMembers))
                }.toList()
            }
        }

    }

    var roleId by GuildRoleTable.rid
    var displayName by GuildRoleTable.displayName
    var guildId by GuildRoleTable.gid
    var systemCreated by GuildRoleTable.systemCreated
    var creator by GuildRoleTable.creator
    var priority by GuildRoleTable.priority
    var permission by GuildRoleTable.permission
    var ownedMembers by GuildRoleTable.ownedMembers

}