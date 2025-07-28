@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalUuidApi::class)

package blora.town.menu

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.town.dao.TownDao
import blora.extension.localization
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import blora.town.TownPermission
import blora.town.TownPermissionContainer
import blora.town.TownTarget
import blora.town.dataprovider.TownSpecificRoleDataProvider
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.style.italic
import plutoproject.adventurekt.text.without
import kotlin.uuid.ExperimentalUuidApi

fun townManagement_permissionManagement_addSpecificRoleMenu(
    menu: Menu,
    guild: GuildDao,
    town: TownDao
): MenuPage<*, *> {
    val townId = town.id
    return pageableMenuPage(menu, TownSpecificRoleDataProvider(guild, town)) {
        pageId {
            "townManagement_${townId}_permissionsManagement_addSpecificRole"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("town", town.displayName)
                }
            ) {
                this.town.menu.town_managementPermissions_managementAdd_specific_roleTitle
            }
        }

        showBackButton()

        dataItem { viewContext, role, dataIndex ->
            icon {
                material { Material.PAPER }
            }
            name {
                mini(role.displayName) without italic
            }
            clickEvent { clickContext ->
                DB.trans {
                    town.permissionContainers = town.permissionContainers.toMutableList().apply {
                        this.add(
                            TownPermissionContainer(
                                systemCreated = false,
                                target = TownTarget.SpecificRole(role.roleId),
                                permissions = TownPermission.allNotSet
                            )
                        )
                    }
                    town.flush()
                }
                clickContext.viewer.send {
                    localization(clickContext.viewer) {
                        this.town.add_specific_roleSuccess
                    }
                }
            }
        }
    }
}