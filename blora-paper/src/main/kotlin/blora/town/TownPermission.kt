package blora.town

import kotlinx.serialization.Serializable

@Serializable
sealed class TownPermission {

    companion object {

        val values: List<TownPermission>
            get() = listOf(
                EnterTown,
                PlaceBlock,
                DestroyBlock,
                Pvp,
                InteractBlock,
                InteractEntity,
                InteractContainer,
                KillOtherMobs,
                KillFriendlyMobs,
                KillHostileMobs,
                CreateResidence,
                DropItem,
                PickupItem,
                PickupExp,
            )

        val allAllow: Map<TownPermission, TownPermissionStatus>
            get() = buildMap {
                for (permission in this@Companion.values) {
                    this[permission] = TownPermissionStatus.ALLOW
                }
            }

        val allDeny: Map<TownPermission, TownPermissionStatus>
            get() = buildMap {
                for (permission in this@Companion.values) {
                    this[permission] = TownPermissionStatus.DENY
                }
            }

        val allNotSet: Map<TownPermission, TownPermissionStatus>
            get() = buildMap {
                for (permission in this@Companion.values) {
                    this[permission] = TownPermissionStatus.NOT_SET
                }
            }

    }

    @Serializable
    object EnterTown : TownPermission()

    @Serializable
    object PlaceBlock : TownPermission()

    @Serializable
    object DestroyBlock : TownPermission()

    @Serializable
    object Pvp : TownPermission()

    @Serializable
    object InteractBlock : TownPermission() // e.g. buttons, pressure plates

    @Serializable
    object InteractEntity : TownPermission() // e.g. villagers

    @Serializable
    object InteractContainer : TownPermission() // e.g. shulker boxes, chests; except ender chest

    @Serializable
    object KillOtherMobs : TownPermission() // e.g. armor stands, item frame

    @Serializable
    object KillFriendlyMobs : TownPermission() // e.g. sheep, cows, pigs

    @Serializable
    object KillHostileMobs : TownPermission() // e.g. zombies, skeletons

    @Serializable
    object CreateResidence : TownPermission()

    @Serializable
    object DropItem : TownPermission()

    @Serializable
    object PickupItem : TownPermission()

    @Serializable
    object PickupExp : TownPermission()

}

enum class TownPermissionStatus {

    ALLOW,
    DENY,
    NOT_SET;

    fun next(): TownPermissionStatus {
        return when (this) {
            ALLOW -> DENY
            DENY -> NOT_SET
            NOT_SET -> ALLOW
        }
    }

    fun nextWithoutNotSet(): TownPermissionStatus {
        return when (this) {
            ALLOW -> DENY
            DENY -> ALLOW
            NOT_SET -> ALLOW
        }
    }

}