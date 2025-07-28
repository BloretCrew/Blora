@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalUuidApi::class)

package blora.town.menu

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.town.dao.TownDao
import blora.extension.localization
import blora.extension.resolvableProfile
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
import blora.town.dataprovider.TownSpecificPlayerDataProvider
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.style.bold
import plutoproject.adventurekt.text.style.italic
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with
import plutoproject.adventurekt.text.without
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

fun townManagement_permissionManagement_addSpecificPlayerMenu(
    menu: Menu,
    guild: GuildDao,
    town: TownDao
): MenuPage<*, *> {
    val townId = town.id
    return pageableMenuPage(menu, TownSpecificPlayerDataProvider(menu.viewer, guild, town)) {
        pageId {
            "townManagement_${townId}_permissionsManagement_addSpecificPlayer"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("town", town.displayName)
                }
            ) {
                this.town.menu.town_managementPermissions_managementAdd_specific_playerTitle
            }
        }

        showBackButton()

        dataItem { viewContext, (playerUuid, playerName), dataIndex ->
            icon {
                material { Material.PLAYER_HEAD }
                DataComponentTypes.PROFILE eq playerUuid.resolvableProfile()
            }
            name {
                text { playerName } without italic with bold
            }
            clickEvent { clickContext ->
                (clickContext.dataProvider as TownSpecificPlayerDataProvider).addFiltered(playerUuid)
                DB.trans {
                    town.permissionContainers = town.permissionContainers.toMutableList().apply {
                        this.add(
                            TownPermissionContainer(
                                systemCreated = false,
                                target = TownTarget.SpecificPlayer(playerUuid.toKotlinUuid()),
                                permissions = TownPermission.allNotSet
                            )
                        )
                    }
                    town.flush()
                }
                clickContext.viewer.send {
                    localization(clickContext.viewer) {
                        this.town.add_specific_playerSuccess
                    }
                }
            }
        }
    }
}