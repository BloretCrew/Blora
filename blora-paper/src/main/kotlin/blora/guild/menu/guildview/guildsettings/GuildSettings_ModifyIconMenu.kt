package blora.guild.menu.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.guild.GuildPermissions
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.inventoryClick
import blora.menu.lines
import blora.menu.menuPage
import blora.menu.title
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send

fun guildSettings_modifyIconMenu(viewer: Player, guild: GuildDao, guilds: MutableList<GuildDao>, permissions: Collection<GuildPermissions>, rerenderGuildViewParent: () -> Unit, cachedItem: ItemStack = guild.parsedIcon.clone()) : SimpleMenuPage {
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {

                }
            ) {
                this.guild.menu.menuGuild_settingsModify_iconTitle
            }
        }
        inventoryClick { item, clickContext ->
            clickContext.stack.replace(guildSettings_modifyIconMenu(
                viewer,
                guild,
                guilds,
                permissions,
                rerenderGuildViewParent,
                item.clone().apply { this.amount = 1 }
            ))
            return@inventoryClick true
        }

        1 to 1 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(viewer) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent {
                it.stack.pop()
            }
        }

        3 to 5 eq {
            icon(cachedItem)
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_settingsModify_iconButtonIcon
                    }
                }
            }
        }

        5 to 9 eq {
            icon(ItemStack(Material.EMERALD))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_settingsModify_iconButtonConfirm
                    }
                }
            }
            clickEvent { menuPageContext ->
                if (guild.parsedIcon != cachedItem) {
                    DB.trans {
                        guild.parsedIcon = cachedItem
                        guild.flush()
                    }
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildUpdateIcon
                        }
                    }
                    menuPageContext.stack.pop()
                    menuPageContext.stack.replace(guildSettingsMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent))
                } else {
                    menuPageContext.stack.pop()
                }
            }
        }
    }
}