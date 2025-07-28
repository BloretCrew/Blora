package blora.town.menu

import blora.database.DB
import blora.database.town.dao.TownDao
import blora.extension.localization
import blora.item.clone
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder

fun townManagement_modifyIconMenu(menu: Menu, town: TownDao): MenuPage<*, *> {
    val townId = town.id
    var cachedIcon = town.parsedIcon
    return limitedDynamicMenuPage(menu) {
        pageId {
            "townManagement_${townId}_modifyIcon"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("town", town.displayName)
                }
            ) {
                this.town.menu.town_managementModify_iconTitle
            }
        }
        inventoryClick { item, clickContext ->
            cachedIcon = item.clone().apply { this.amount = 1 }
            clickContext.menu.rerender()
            return@inventoryClick true
        }

        backButton()

        3 to 5 eq {
            icon { clone { cachedIcon } }
            name {
                localization(menu.viewer) {
                    this.town.menu.town_managementModify_iconButtonIcon
                }
            }
        }

        5 to 9 eq {
            icon { material { Material.EMERALD } }
            name {
                localization(menu.viewer) {
                    this.town.menu.town_managementModify_iconButtonConfirm
                }
            }
            clickEvent { clickContext ->
                DB.trans {
                    town.refresh()
                }
                if (town.parsedIcon != cachedIcon) {
                    DB.trans {
                        town.parsedIcon = cachedIcon
                        town.flush()
                    }
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.town.iconUpdate
                        }
                    }
                }
                clickContext.stack.pop()
            }
        }
    }
}