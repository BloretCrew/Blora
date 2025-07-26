package blora.guild.menu.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.guild.GuildPermissions
import blora.item.clone
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.inventoryClick
import blora.menu.v2.page.builder.limitedDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildSettings_modifyIconMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    var cachedIcon = guild.parsedIcon
    return limitedDynamicMenuPage(menu) {
        pageId {
            "guild_${guildId}_settings_modifyIcon"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_settingsModify_iconTitle
            }
        }
        inventoryClick { item, clickContext ->
            cachedIcon = item.clone().apply { this.amount = 1}
            clickContext.menu.rerender()
            return@inventoryClick true
        }

        backButton()

        3 to 5 eq {
            icon { clone { cachedIcon } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_settingsModify_iconButtonIcon
                }
            }
        }

        5 to 9 eq {
            icon { material { Material.EMERALD } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_settingsModify_iconButtonConfirm
                }
            }
            clickEvent { clickContext ->
                DB.trans {
                    guild.refresh()
                }
                val isMember = guild.members.contains(menu.viewer.uniqueId)
                val permissions = guild.getPlayerPermissions(menu.viewer.uniqueId, isMember)
                if (permissions.contains(GuildPermissions.MODIFY_GUILD_ICON)) {
                    if (guild.parsedIcon != cachedIcon) {
                        DB.trans {
                            guild.parsedIcon = cachedIcon
                            guild.flush()
                        }
                        clickContext.viewer.send {
                            localization(clickContext.viewer) {
                                this.guild.guildUpdateIcon
                            }
                        }
                    }
                }
                clickContext.stack.pop()
            }
        }
    }
}