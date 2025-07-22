package blora.town

import kotlinx.serialization.Serializable


@Serializable
data class TownPermissionContainer(
    val target: TownTarget,
    val permissions: Map<TownPermission, TownPermissionStatus>
)