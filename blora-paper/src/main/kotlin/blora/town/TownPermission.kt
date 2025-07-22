package blora.town

import kotlinx.serialization.Serializable

@Serializable
sealed class TownPermission {

    object EnterTown : TownPermission()
    object PlaceBlock : TownPermission()
    object DestroyBlock : TownPermission()

}

enum class TownPermissionStatus {

    ALLOW,
    DENY,
    NOT_SET

}