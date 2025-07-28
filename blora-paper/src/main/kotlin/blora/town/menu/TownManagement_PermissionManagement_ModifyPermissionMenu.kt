@file:OptIn(ExperimentalUuidApi::class)
@file:Suppress("UnstableApiUsage")

package blora.town.menu

import blora.database.DB
import blora.database.guild.dao.GuildRoleDao
import blora.database.town.dao.TownDao
import blora.extension.localization
import blora.extension.resolvableProfile
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.limitedDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import blora.town.TownPermission
import blora.town.TownPermissionContainer
import blora.town.TownPermissionStatus
import blora.town.TownTarget
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.parsedPlaceholder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

fun townManagement_permissionManagement_modifyPermissionMenu(
    menu: Menu,
    town: TownDao,
    townPermissionContainer: TownPermissionContainer,
    dataIndex: Int
): MenuPage<*, *> {
    val townId = town.id
    val cachedPermissions = townPermissionContainer.permissions.toMutableMap()
    return limitedDynamicMenuPage(menu) {
        pageId {
            "townManagement_${townId}_permissionsManagement_modifyPermission_${townPermissionContainer.hashCode()}"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("town", town.displayName)
                }
            ) {
                this.town.menu.town_managementPermissions_managementModify_permissionTitle
            }
        }
        backButton()

        1 to 5 eq { viewContext ->
            when (townPermissionContainer.target) {
                TownTarget.Blocked -> {
                    icon {
                        material { Material.BLACK_DYE }
                    }
                    name {
                        localization(viewContext.viewer) {
                            this.town.menu.town_managementPermissions_managementTargetBlocked
                        }
                    }
                }

                TownTarget.Ally -> {
                    icon {
                        material { Material.ALLAY_SPAWN_EGG }
                    }
                    name {
                        localization(viewContext.viewer) {
                            this.town.menu.town_managementPermissions_managementTargetAlly
                        }
                    }
                }

                TownTarget.Member -> {
                    icon {
                        material { Material.TOTEM_OF_UNDYING }
                    }
                    name {
                        localization(viewContext.viewer) {
                            this.town.menu.town_managementPermissions_managementTargetMember
                        }
                    }
                }

                TownTarget.Public -> {
                    icon {
                        material { Material.MOJANG_BANNER_PATTERN }
                    }
                    name {
                        localization(viewContext.viewer) {
                            this.town.menu.town_managementPermissions_managementTargetPublic
                        }
                    }
                }

                is TownTarget.SpecificPlayer -> {
                    icon {
                        material { Material.PLAYER_HEAD }
                        DataComponentTypes.PROFILE eq townPermissionContainer.target.uuid.toJavaUuid()
                            .resolvableProfile()
                    }
                    name {
                        localization(
                            viewContext.viewer,
                            tags = {
                                parsedPlaceholder(
                                    "player",
                                    DB.getPlayerDisplayName(
                                        townPermissionContainer.target.uuid.toJavaUuid()
                                    )
                                )
                            }
                        ) {
                            this.town.menu.town_managementPermissions_managementTargetSpecific_player
                        }
                    }
                }

                is TownTarget.SpecificRole -> {
                    icon {
                        material { Material.PAPER }
                    }
                    name {
                        localization(
                            viewContext.viewer,
                            tags = {
                                componentPlaceholder("role") {
                                    localization(viewContext.viewer) {
                                        GuildRoleDao.find(town.guildId, townPermissionContainer.target.roleId)
                                            ?.displayName
                                            ?: this.town.menu.town_managementPermissions_managementTargetSpecific_roleUnknown
                                    }
                                }
                            }
                        ) {
                            this.town.menu.town_managementPermissions_managementTargetSpecific_role
                        }
                    }
                }
            }
        }

        3 to 2 eq { viewContext ->
            icon { material { Material.LEATHER_BOOTS } }
            name {
                localization(menu.viewer) {
                    this.town.permissionEnterTown
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.EnterTown]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.EnterTown]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.EnterTown] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        3 to 3 eq { viewContext ->
            icon { material { Material.STONE } }
            name {
                localization(menu.viewer) {
                    this.town.permissionPlaceBlock
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.PlaceBlock]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.PlaceBlock]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.PlaceBlock] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        3 to 4 eq { viewContext ->
            icon { material { Material.IRON_PICKAXE } }
            name {
                localization(menu.viewer) {
                    this.town.permissionDestroyBlock
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.DestroyBlock]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.DestroyBlock]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.DestroyBlock] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        3 to 5 eq { viewContext ->
            icon { material { Material.GOLDEN_SWORD } }
            name {
                localization(menu.viewer) {
                    this.town.permissionPvp
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.Pvp]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.Pvp]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.Pvp] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        3 to 6 eq { viewContext ->
            icon { material { Material.OAK_BUTTON } }
            name {
                localization(menu.viewer) {
                    this.town.permissionInteractBlock
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.InteractBlock]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.InteractBlock]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.InteractBlock] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        3 to 7 eq { viewContext ->
            icon { material { Material.CARROT_ON_A_STICK } }
            name {
                localization(menu.viewer) {
                    this.town.permissionInteractEntity
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.InteractEntity]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.InteractEntity]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.InteractEntity] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        3 to 8 eq { viewContext ->
            icon { material { Material.CHEST } }
            name {
                localization(menu.viewer) {
                    this.town.permissionInteractContainer
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.InteractContainer]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.InteractContainer]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.InteractContainer] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        4 to 2 eq { viewContext ->
            icon { material { Material.ARMOR_STAND } }
            name {
                localization(menu.viewer) {
                    this.town.permissionKillOtherMobs
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.KillOtherMobs]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.KillOtherMobs]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.KillOtherMobs] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        4 to 3 eq { viewContext ->
            icon { material { Material.PORKCHOP } }
            name {
                localization(menu.viewer) {
                    this.town.permissionKillFriendlyMobs
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.KillFriendlyMobs]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.KillFriendlyMobs]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.KillFriendlyMobs] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        4 to 4 eq { viewContext ->
            icon { material { Material.ZOMBIE_HEAD } }
            name {
                localization(menu.viewer) {
                    this.town.permissionKillHostileMobs
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.KillHostileMobs]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.KillHostileMobs]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.KillHostileMobs] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        4 to 5 eq { viewContext ->
            icon { material { Material.WOODEN_HOE } }
            name {
                localization(menu.viewer) {
                    this.town.permissionCreateResidence
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.CreateResidence]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.CreateResidence]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.CreateResidence] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        4 to 6 eq { viewContext ->
            icon { material { Material.DROPPER } }
            name {
                localization(menu.viewer) {
                    this.town.permissionDropItem
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.DropItem]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.DropItem]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.DropItem] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        4 to 7 eq { viewContext ->
            icon { material { Material.HOPPER } }
            name {
                localization(menu.viewer) {
                    this.town.permissionPickupItem
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.PickupItem]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.PickupItem]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.PickupItem] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        4 to 8 eq { viewContext ->
            icon { material { Material.EXPERIENCE_BOTTLE } }
            name {
                localization(menu.viewer) {
                    this.town.permissionPickupExp
                }
            }
            description {
                localization(menu.viewer) {
                    val permissionStatus = cachedPermissions[TownPermission.PickupExp]
                    if (permissionStatus == TownPermissionStatus.ALLOW) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemAllow
                    } else if (permissionStatus == TownPermissionStatus.DENY) {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemDeny
                    } else {
                        this.town.menu.town_managementPermissions_managementModify_permissionItemNot_set
                    }
                }
            }
            clickEvent {
                val permissionStatus = cachedPermissions[TownPermission.PickupExp]
                    ?: (if (townPermissionContainer.systemCreated) TownPermissionStatus.DENY else TownPermissionStatus.NOT_SET)
                cachedPermissions[TownPermission.PickupExp] = if (townPermissionContainer.systemCreated)
                    permissionStatus.next()
                else
                    permissionStatus.nextWithoutNotSet()
                it.menu.rerender()
            }
        }

        5 to 9 eq { viewContext ->
            icon { material { Material.EMERALD } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementPermissions_managementModify_permissionButtonConfirm
                }
            }
            clickEvent { clickContext ->
                DB.trans {
                    town.permissionContainers = town.permissionContainers.toMutableList()
                        .apply {
                            this[dataIndex] = TownPermissionContainer(
                                townPermissionContainer.systemCreated,
                                townPermissionContainer.target,
                                cachedPermissions.toMap()
                            )
                        }
                    town.flush()
                }
                clickContext.viewer.send {
                    localization(clickContext.viewer) {
                        this.town.modify_permissionSuccess
                    }
                }
                clickContext.stack.pop()
            }
        }
    }
}