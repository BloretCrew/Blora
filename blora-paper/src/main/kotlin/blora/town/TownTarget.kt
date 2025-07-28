@file:OptIn(ExperimentalUuidApi::class)

package blora.town

import blora.database.guild.dao.GuildDao
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@Serializable
sealed class TownTarget {

    // priority: SpecificPlayer > Blocked > SpecificRole > Member > Ally > Public

    abstract fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus>

    @Serializable
    class SpecificRole(val roleId: String) : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return Member.defaultPermissions(townId)
        }
    }

    @Serializable
    class SpecificPlayer(val uuid: Uuid) : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            val guild = GuildDao.getByTownId(townId)
            return if (guild == null || !guild.members.contains(uuid.toJavaUuid())) {
                Public.defaultPermissions(townId)
            } else {
                Member.defaultPermissions(townId)
            }
        }
    }

    @Serializable
    object Member : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return mapOf(
                TownPermission.EnterTown to TownPermissionStatus.ALLOW,
                TownPermission.PlaceBlock to TownPermissionStatus.ALLOW,
                TownPermission.DestroyBlock to TownPermissionStatus.ALLOW,
                TownPermission.Pvp to TownPermissionStatus.ALLOW,
                TownPermission.InteractBlock to TownPermissionStatus.ALLOW,
                TownPermission.InteractEntity to TownPermissionStatus.ALLOW,
                TownPermission.InteractContainer to TownPermissionStatus.ALLOW,
                TownPermission.KillOtherMobs to TownPermissionStatus.ALLOW,
                TownPermission.KillFriendlyMobs to TownPermissionStatus.ALLOW,
                TownPermission.KillHostileMobs to TownPermissionStatus.ALLOW,
                TownPermission.CreateResidence to TownPermissionStatus.DENY,
                TownPermission.DropItem to TownPermissionStatus.ALLOW,
                TownPermission.PickupItem to TownPermissionStatus.ALLOW,
                TownPermission.PickupExp to TownPermissionStatus.ALLOW
            )
        }
    }

    @Serializable
    object Public : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return mapOf(
                TownPermission.EnterTown to TownPermissionStatus.ALLOW,
                TownPermission.PlaceBlock to TownPermissionStatus.DENY,
                TownPermission.DestroyBlock to TownPermissionStatus.DENY,
                TownPermission.Pvp to TownPermissionStatus.DENY,
                TownPermission.InteractBlock to TownPermissionStatus.DENY,
                TownPermission.InteractEntity to TownPermissionStatus.DENY,
                TownPermission.InteractContainer to TownPermissionStatus.DENY,
                TownPermission.KillOtherMobs to TownPermissionStatus.DENY,
                TownPermission.KillFriendlyMobs to TownPermissionStatus.DENY,
                TownPermission.KillHostileMobs to TownPermissionStatus.DENY,
                TownPermission.CreateResidence to TownPermissionStatus.DENY,
                TownPermission.DropItem to TownPermissionStatus.DENY,
                TownPermission.PickupItem to TownPermissionStatus.DENY,
                TownPermission.PickupExp to TownPermissionStatus.DENY
            )
        }
    }

    @Serializable
    object Ally : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return mapOf(
                TownPermission.EnterTown to TownPermissionStatus.ALLOW,
                TownPermission.PlaceBlock to TownPermissionStatus.ALLOW,
                TownPermission.DestroyBlock to TownPermissionStatus.ALLOW,
                TownPermission.Pvp to TownPermissionStatus.DENY,
                TownPermission.InteractBlock to TownPermissionStatus.ALLOW,
                TownPermission.InteractEntity to TownPermissionStatus.ALLOW,
                TownPermission.InteractContainer to TownPermissionStatus.ALLOW,
                TownPermission.KillOtherMobs to TownPermissionStatus.DENY,
                TownPermission.KillFriendlyMobs to TownPermissionStatus.DENY,
                TownPermission.KillHostileMobs to TownPermissionStatus.DENY,
                TownPermission.CreateResidence to TownPermissionStatus.DENY,
                TownPermission.DropItem to TownPermissionStatus.ALLOW,
                TownPermission.PickupItem to TownPermissionStatus.ALLOW,
                TownPermission.PickupExp to TownPermissionStatus.ALLOW
            )
        }
    }

    @Serializable
    object Blocked : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return mapOf(
                TownPermission.EnterTown to TownPermissionStatus.DENY,
                TownPermission.PlaceBlock to TownPermissionStatus.DENY,
                TownPermission.DestroyBlock to TownPermissionStatus.DENY,
                TownPermission.Pvp to TownPermissionStatus.DENY,
                TownPermission.InteractBlock to TownPermissionStatus.DENY,
                TownPermission.InteractEntity to TownPermissionStatus.DENY,
                TownPermission.InteractContainer to TownPermissionStatus.DENY,
                TownPermission.KillOtherMobs to TownPermissionStatus.DENY,
                TownPermission.KillFriendlyMobs to TownPermissionStatus.DENY,
                TownPermission.KillHostileMobs to TownPermissionStatus.DENY,
                TownPermission.CreateResidence to TownPermissionStatus.DENY,
                TownPermission.DropItem to TownPermissionStatus.DENY,
                TownPermission.PickupItem to TownPermissionStatus.DENY,
                TownPermission.PickupExp to TownPermissionStatus.DENY
            )
        }
    }

}