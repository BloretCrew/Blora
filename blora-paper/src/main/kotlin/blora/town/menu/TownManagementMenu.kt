package blora.town.menu

import blora.database.guild.dao.GuildDao
import blora.database.town.dao.TownDao
import blora.extension.localization
import blora.extension.openDialog
import blora.item.clone
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.limitedDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import blora.town.dialog.townManagement_modifyGoodbyeMessageDialog
import blora.town.dialog.townManagement_modifyNameDialog
import blora.town.dialog.townManagement_modifyWelcomeMessageDialog
import org.bukkit.Material
import plutoproject.adventurekt.text.parsedPlaceholder

fun townManagementMenu(menu: Menu, guild: GuildDao, town: TownDao): MenuPage<*, *> {
    val townId = town.id
    return limitedDynamicMenuPage(menu) {

        pageId {
            "townManagement_$townId"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("town", town.displayName)
                }
            ) {
                this.town.menu.town_managementTitle
            }
        }

        backButton()

        1 to 5 eq {
            icon { clone { town.parsedIcon } }
            name {
                localization(menu.viewer) {
                    town.displayName
                }
            }
        }

        3 to 2 eq { viewContext ->
            // modify icon
            icon { material { Material.SCULK } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementButtonModify_icon
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.push {
                    townManagement_modifyIconMenu(clickContext.menu, town)
                }
            }
        }

        3 to 5 eq { viewContext ->
            // modify name
            icon { material { Material.NAME_TAG } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementButtonModify_name
                }
            }
            clickEvent { clickContext ->
                clickContext.viewer.openDialog(
                    townManagement_modifyNameDialog(clickContext.viewer, town) {
                        clickContext.menu.rerender()
                    }
                )
            }
        }

        3 to 8 eq { viewContext ->
            // modify welcome message
            icon { material { Material.HONEY_BOTTLE } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementButtonModify_welcome_message
                }
            }
            clickEvent { clickContext ->
                clickContext.viewer.openDialog(
                    townManagement_modifyWelcomeMessageDialog(clickContext.viewer, town) {
                        clickContext.menu.rerender()
                    }
                )
            }
        }

        4 to 2 eq { viewContext ->
            // modify goodbye message
            icon { material { Material.OMINOUS_BOTTLE } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementButtonModify_goodbye_message
                }
            }
            clickEvent { clickContext ->
                clickContext.viewer.openDialog(
                    townManagement_modifyGoodbyeMessageDialog(clickContext.viewer, town) {
                        clickContext.menu.rerender()
                    }
                )
            }
        }

        4 to 5 eq { viewContext ->
            // modify chunks (important)
            icon { material { Material.DIRT } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementButtonChunks_management
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.push {
                    townChunksManagementMenu(clickContext.menu, guild, town)
                }
            }
        }

        4 to 8 eq { viewContext ->
            // modify user permissions (important)
            icon { material { Material.COMMAND_BLOCK } }
            name {
                localization(viewContext.viewer) {
                    this.town.menu.town_managementButtonPermission_management
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.push {
                    townManagement_permissionManagement(clickContext.menu, guild, town)
                }
            }
        }
    }
}