@file:OptIn(ExperimentalUuidApi::class)

package blora.town

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
sealed class TownTarget {

    // priority: SpecificTarget > Blocked > SpecificRole > Member > Ally > Public

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
            // TODO: check player is town member, if so return member default permissions
            return Public.defaultPermissions(townId)
        }
    }

    @Serializable
    object Member : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return mapOf()
        }
    }

    @Serializable
    object Public : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return mapOf()
        }
    }

    @Serializable
    object Ally : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return mapOf()
        }
    }

    @Serializable
    object Blocked : TownTarget() {
        override fun defaultPermissions(townId: String): Map<TownPermission, TownPermissionStatus> {
            return mapOf()
        }
    }

}

@Serializable
data class TownPermissionSettings(
    val townId: String,
    val containers: List<TownPermissionContainer> = listOf(
        TownPermissionContainer(
            TownTarget.Member,
            TownTarget.Member.defaultPermissions(townId)
        ),
        TownPermissionContainer(
            TownTarget.Public,
            TownTarget.Public.defaultPermissions(townId)
        ),
        TownPermissionContainer(
            TownTarget.Ally,
            TownTarget.Ally.defaultPermissions(townId)
        ),
        TownPermissionContainer(
            TownTarget.Blocked,
            TownTarget.Blocked.defaultPermissions(townId)
        )
    ),
)