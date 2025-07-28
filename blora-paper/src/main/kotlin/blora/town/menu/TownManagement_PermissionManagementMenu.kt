@file:OptIn(ExperimentalUuidApi::class)
@file:Suppress("UnstableApiUsage")

package blora.town.menu

import blora.database.DB
import blora.database.guild.dao.GuildDao
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
import blora.menu.v2.page.builder.*
import blora.town.TownTarget
import blora.town.dataprovider.TownTargetDataProvider
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

fun townManagement_permissionManagement(menu: Menu, guild: GuildDao, town: TownDao): MenuPage<*, *> {
    val townId = town.id
    return pageableMenuPage(menu, TownTargetDataProvider(town)) {
        pageId {
            "townManagement_${townId}_permissionsManagement"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("town", town.displayName)
                }
            ) {
                this.town.menu.town_managementPermissions_managementTitle
            }
        }

        showBackButton()

        5 to 4 eq { viewContext ->
            icon { material { Material.PLAYER_HEAD } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementPermissions_managementButtonAdd_specific_player
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.push {
                    townManagement_permissionManagement_addSpecificPlayerMenu(menu, guild, town)
                }
            }
        }

        5 to 6 eq { viewContext ->
            icon { material { Material.LEATHER_HELMET } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementPermissions_managementButtonAdd_specific_role
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.push {
                    townManagement_permissionManagement_addSpecificRoleMenu(menu, guild, town)
                }
            }
        }

        dataItem { viewContext, townPermissionContainer, dataIndex ->
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
            description {
                newline()
                localization(viewContext.viewer) {
                    this.town.menu.town_managementPermissions_managementItemDescriptionLeft
                }
                if (!townPermissionContainer.systemCreated) {
                    newline()
                    localization(viewContext.viewer) {
                        this.town.menu.town_managementPermissions_managementItemDescriptionRight
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isLeftClick) {
                    clickContext.stack.push {
                        townManagement_permissionManagement_modifyPermissionMenu(
                            menu,
                            town,
                            townPermissionContainer,
                            dataIndex
                        )
                    }
                } else if (clickContext.click.isRightClick) {
                    if (townPermissionContainer.systemCreated)
                        return@clickEvent
                    (clickContext.dataProvider as TownTargetDataProvider).addFilter(townPermissionContainer.target)
                    DB.trans {
                        town.permissionContainers = town.permissionContainers.toMutableList()
                            .apply { removeAt(dataIndex) }
                        town.flush()
                    }
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.town.permission_targetDelete
                        }
                    }
                }
            }
        }
    }
}