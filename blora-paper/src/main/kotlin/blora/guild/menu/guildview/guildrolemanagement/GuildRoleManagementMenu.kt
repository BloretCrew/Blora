package blora.guild.menu.guildview.guildrolemanagement

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.dataprovider.GuildRoleDaoDataProvider
import blora.guild.dialog.guildview.guildrolemanagement.guildRoleManagemenet_createNewRoleDialog
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildRoleManagementMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, GuildRoleDaoDataProvider(guild.gid)) {
        pageId {
            "guild_${guildId}_roleManagement"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_role_managementTitle
            }
        }
        showBackButton()

        5 to 5 eq {
            icon { material { Material.APPLE } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_role_managementButtonCreate_role
                }
            }
            clickEvent { clickContext ->
                clickContext.viewer.openDialog(
                    guildRoleManagemenet_createNewRoleDialog(
                        clickContext.viewer,
                        guild,
                        { role ->
                            clickContext.menu.rerender()
                        }
                    )
                )
            }
        }

        dataItem { viewContext, role, dataIndex ->
            icon { material { Material.PAPER } }
            name {
                localization(viewContext.viewer) {
                    role.displayName
                }
            }
            description {
                newline()
                localization(viewContext.viewer) {
                    this.guild.menu.menuGuild_role_managementItemTooltip1
                }
                if (!role.systemCreated) {
                    newline()
                    localization(viewContext.viewer) {
                        this.guild.menu.menuGuild_role_managementItemTooltip2
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isLeftClick) {
                    clickContext.stack.push {
                        guildRoleManagement_modifyRoleMenu(menu, guild, role)
                    }
                } else if (clickContext.click.isRightClick) {
                    DB.trans {
                        guild.refresh()
                    }
                    if (guild.owner != clickContext.viewer.uniqueId) {
                        clickContext.stack.pop()
                        return@clickEvent
                    }
                    DB.trans {
                        role.delete()
                    }
                    clickContext.viewer.send {
                        localization(
                            player = clickContext.viewer,
                            tags = {
                                parsedPlaceholder("role", role.displayName)
                            }
                        ) {
                            this.guild.guildRoleDelete_success
                        }
                    }
                    clickContext.menu.rerender()
                }
            }
        }
    }
}