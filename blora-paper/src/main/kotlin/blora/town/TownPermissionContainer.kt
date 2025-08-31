package blora.town

import kotlinx.serialization.Serializable


@Serializable
data class TownPermissionContainer(
    val systemCreated: Boolean,
    val target: TownTarget,
    val permissions: Map<TownPermission, TownPermissionStatus>
) {

    companion object {

        fun defaultAllyPermissionContainer(): TownPermissionContainer {
            return TownPermissionContainer(
                true,
                TownTarget.Ally,
                TownTarget.Ally.defaultPermissions("")
            )
        }

        fun defaultBlockedPermissionContainer(): TownPermissionContainer {
            return TownPermissionContainer(
                true,
                TownTarget.Blocked,
                TownTarget.Blocked.defaultPermissions("")
            )
        }

        fun defaultPublicPermissionContainer(): TownPermissionContainer {
            return TownPermissionContainer(
                true,
                TownTarget.Public,
                TownTarget.Public.defaultPermissions("")
            )
        }

        fun defaultMemberPermissionContainer(): TownPermissionContainer {
            return TownPermissionContainer(
                true,
                TownTarget.Member,
                TownTarget.Member.defaultPermissions("")
            )
        }

    }

}