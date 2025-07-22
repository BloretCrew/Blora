package blora.guild.menu.guildview.guildrolemanagement

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildRoleDao
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.GuildPermissions
import blora.guild.dialog.guildview.guildrolemanagement.guildRoleManagemenet_createNewRole
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.mapping
import blora.menu.menuPage
import blora.menu.title
import blora.plugin.BloraPlugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildRoleManagementMenu(viewer: Player, guild: GuildDao, permissions: Collection<GuildPermissions>, cachedRoles: MutableList<GuildRoleDao>? = null, currentPage: Int = 1): SimpleMenuPage {
    val roles = cachedRoles ?: BloraPlugin.database.listRolesForGuild(guild.gid).toMutableList()
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_role_managementTitle
            }
        }

        mapping(
            "#########",
            "#       #",
            "#       #",
            "#       #",
            "#########",
        )

        '#' eq {
            icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
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

        if (roles.size <= (currentPage - 1) * 21 - 1)
            return@menuPage

        if (currentPage > 1) {
            5 to 1 eq {
                icon(ItemStack(Material.ARROW))
                hoverText {
                    title {
                        localization(viewer) {
                            this.menuButtonPrevious_page
                        }
                    }
                }
                clickEvent {
                    it.stack.pop()
                    it.stack.push(guildRoleManagementMenu(viewer, guild, permissions, roles,  currentPage - 1))
                }
            }
        }

        if (roles.size > (currentPage * 21)) {
            5 to 9 eq {
                icon(ItemStack(Material.ARROW))
                hoverText {
                    title {
                        localization(viewer) {
                            this.menuButtonNext_page
                        }
                    }
                }
                clickEvent {
                    it.stack.pop()
                    it.stack.push(guildRoleManagementMenu(viewer, guild, permissions, roles,  currentPage + 1))
                }
            }
        }

        5 to 5 eq {
            icon(ItemStack(Material.APPLE))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_role_managementButtonCreate_role
                    }
                }
            }
            clickEvent {
                viewer.openDialog(
                    guildRoleManagemenet_createNewRole(
                        viewer,
                        guild,
                        { role ->
                            roles.add(role)
                            roles.sortedWith { first, second ->
                                val priorityCompare = first.priority.compareTo(second.priority)
                                if (priorityCompare != 0)
                                    return@sortedWith priorityCompare
                                return@sortedWith first.roleId.compareTo(second.roleId)
                            }
                            it.stack.pop()
                            it.stack.push(guildRoleManagementMenu(viewer, guild, permissions, roles,  currentPage))
                        }
                    )
                )
            }
        }

        roles.forEachIndexed { index, role ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(ItemStack(Material.PAPER))
                hoverText {
                    title {
                        localization(viewer) {
                            role.displayName
                        }
                    }
                    description {
                        newline()
                        localization(viewer) {
                            this.guild.menu.menuGuild_role_managementItemTooltip1
                        }
                        if (!role.systemCreated) {
                            newline()
                            localization(viewer) {
                                this.guild.menu.menuGuild_role_managementItemTooltip2
                            }
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    if (menuPageContext.clickType.isLeftClick) {
                        menuPageContext.stack.push(guildRoleManagement_modifyRoleMenu(viewer, guild, role))
                    } else if (menuPageContext.clickType.isRightClick && !role.systemCreated) {
                        DB.trans {
                            role.delete()
                        }
                        roles.remove(role)
                        roles.sortedWith { first, second ->
                            val priorityCompare = first.priority.compareTo(second.priority)
                            if (priorityCompare != 0)
                                return@sortedWith priorityCompare
                            return@sortedWith first.roleId.compareTo(second.roleId)
                        }
                        if (roles.size <= (currentPage - 1) * 21 - 1) {
                            menuPageContext.stack.replace(guildRoleManagementMenu(viewer, guild, permissions, roles, currentPage - 1))
                        } else {
                            menuPageContext.stack.replace(guildRoleManagementMenu(viewer, guild, permissions, roles, currentPage))
                        }
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("role", role.displayName)
                                }
                            ) {
                                this.guild.guildRoleDelete_success
                            }
                        }
                    }
                }
            }
        }
    }
}