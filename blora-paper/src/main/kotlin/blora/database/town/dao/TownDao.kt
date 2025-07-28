@file:OptIn(ExperimentalUuidApi::class)

package blora.database.town.dao

import blora.converter.jsonToItemStack
import blora.converter.toJson
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildRoleDao
import blora.database.town.table.TownTable
import blora.permission.Permissions
import blora.town.TownPermission
import blora.town.TownPermissionStatus
import blora.town.TownTarget
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

class TownDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<TownDao>(TownTable) {

        fun listAll(): List<TownDao> {
            return DB.trans {
                all().toList()
            }
        }

        fun list(guild: String): List<TownDao> {
            return DB.trans {
                find {
                    TownTable.guildId eq guild
                }.toList()
            }
        }

        fun getByTownId(town: String): TownDao? {
            return DB.trans {
                find {
                    TownTable.townId eq town
                }.firstOrNull()
            }
        }

    }

    var townId by TownTable.townId
    var guildId by TownTable.guildId

    var icon by TownTable.icon
    var displayName by TownTable.displayName

    var welcomeMessage by TownTable.welcomeMessage
    var goodbyeMessage by TownTable.goodbyeMessage

    var world by TownTable.world
    var centerChunkX by TownTable.centerChunkX
    var centerChunkZ by TownTable.centerChunkZ

    var creator by TownTable.creator
    var createdAt by TownTable.createdAt

    var permissionContainers by TownTable.permissionContainers

    var parsedIcon: ItemStack
        get() = this.icon.jsonToItemStack()
        set(value) {
            this.icon = value.toJson()
        }

    fun listChunks(): List<TownChunkDao> {
        return TownChunkDao.listByTown(this.townId)
    }

    fun getPlayerPermission(player: UUID): Map<TownPermission, TownPermissionStatus> {
        val onlinePlayer = Bukkit.getPlayer(player)
        if (onlinePlayer != null && onlinePlayer.hasPermission(Permissions.Admin))
            return TownPermission.allAllow

        val guild = GuildDao.getByGuildId(this.guildId)
        if (guild == null)
            return TownPermission.allDeny

        if (guild.owner == player)
            return TownPermission.allAllow

        val permissions = TownPermission.allNotSet.toMutableMap()

        val specificPlayer =
            this.permissionContainers.find { it.target is TownTarget.SpecificPlayer && it.target.uuid.toJavaUuid() == player }
        if (specificPlayer != null) {
            for ((permission, status) in specificPlayer.permissions) {
                if (status == TownPermissionStatus.NOT_SET)
                    continue
                permissions[permission] = status
            }
        }
        if (guild.blocklist.contains(player)) {
            for ((permission, status) in this.permissionContainers.find { it == TownTarget.Blocked }!!.permissions) {
                if (status == TownPermissionStatus.NOT_SET)
                    continue
                permissions[permission] = status
            }
        }
        if (guild.members.contains(player)) {
            val roles = GuildRoleDao.listByGuildIdAndOwnedMember(this.guildId, player)
                .sortedWith { first, second ->
                    val priorityCompare = first.priority.compareTo(second.priority)
                    if (priorityCompare != 0)
                        return@sortedWith -priorityCompare
                    return@sortedWith first.roleId.compareTo(second.roleId)
                }

            for (role in roles) {
                val specificRole =
                    this.permissionContainers.find { it.target is TownTarget.SpecificRole && it.target.roleId == role.roleId }
                if (specificRole != null) {
                    for ((permission, status) in specificRole.permissions) {
                        if (status == TownPermissionStatus.NOT_SET)
                            continue
                        permissions[permission] = status
                    }
                }
            }
            for ((permission, status) in this.permissionContainers.find { it == TownTarget.Member }!!.permissions) {
                if (status == TownPermissionStatus.NOT_SET)
                    continue
                permissions[permission] = status
            }
        }
        if (GuildDao.isPlayerInAnyGuild(player, guild.allys)) {
            for ((permission, status) in this.permissionContainers.find { it == TownTarget.Ally }!!.permissions) {
                if (status == TownPermissionStatus.NOT_SET)
                    continue
                permissions[permission] = status
            }
        }
        for ((permission, status) in this.permissionContainers.find { it == TownTarget.Public }!!.permissions) {
            if (status == TownPermissionStatus.NOT_SET)
                continue
            permissions[permission] = status
        }

        return permissions.toMap()
    }

}