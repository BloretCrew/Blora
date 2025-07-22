package blora.guild.menu.guildview.guildrolemanagement

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildRoleDao
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.dialog.guildview.guildsettings.guildRoleManagement_modifyRole_modifyNameDialog
import blora.guild.role.RolePermissions
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.menuPage
import blora.menu.title
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildRoleManagement_modifyRoleMenu(viewer: Player, guild: GuildDao, role: GuildRoleDao, cachedPermissions: RolePermissions = role.permission.clone()): SimpleMenuPage {
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                    parsedPlaceholder("role", role.displayName)
                }
            ) {
                this.guild.menu.menuGuild_role_managementModify_roleTitle
            }
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

        1 to 5 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_role_managementModify_roleButtonModify_name
                    }
                }
            }
            clickEvent {
                viewer.openDialog(
                    guildRoleManagement_modifyRole_modifyNameDialog(viewer, role, it.menuContext)
                )
            }
        }

        3 to 2 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionModify_guild_name
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.modifyGuildName) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.modifyGuildName = !cachedPermissions.modifyGuildName
                it.menu.rerender()
            }
        }
        3 to 3 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionModify_guild_icon
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.modifyGuildIcon) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.modifyGuildIcon = !cachedPermissions.modifyGuildIcon
                it.menu.rerender()
            }
        }
        3 to 4 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionModify_guild_visibility
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.modifyGuildVisibility) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.modifyGuildVisibility = !cachedPermissions.modifyGuildVisibility
                it.menu.rerender()
            }
        }
        3 to 5 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionModify_guild_join_strategy
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.modifyGuildJoinStrategy) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.modifyGuildJoinStrategy = !cachedPermissions.modifyGuildJoinStrategy
                it.menu.rerender()
            }
        }
        3 to 6 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionKick_player
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.kickPlayer) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.kickPlayer = !cachedPermissions.kickPlayer
                it.menu.rerender()
            }
        }
        3 to 7 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionReview_player
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.reviewPlayer) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.reviewPlayer = !cachedPermissions.reviewPlayer
                it.menu.rerender()
            }
        }
        3 to 8 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionInvite_player
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.invitePlayer) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.invitePlayer = !cachedPermissions.invitePlayer
                it.menu.rerender()
            }
        }
        4 to 2 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionRequest_ally
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.requestAlly) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.requestAlly = !cachedPermissions.requestAlly
                it.menu.rerender()
            }
        }
        4 to 3 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionReview_ally
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.reviewAlly) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.reviewAlly = !cachedPermissions.reviewAlly
                it.menu.rerender()
            }
        }
        4 to 4 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionStop_ally
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.stopAlly) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.stopAlly = !cachedPermissions.stopAlly
                it.menu.rerender()
            }
        }
        4 to 5 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionManage_invitation_code
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.manageInvitationCode) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.manageInvitationCode = !cachedPermissions.manageInvitationCode
                it.menu.rerender()
            }
        }
        4 to 6 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionStore_bank
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.storeBank) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.storeBank = !cachedPermissions.storeBank
                it.menu.rerender()
            }
        }
        4 to 7 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionWithdraw_bank
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.withdrawBank) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.withdrawBank = !cachedPermissions.withdrawBank
                it.menu.rerender()
            }
        }
        4 to 8 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildRole_permissionUse_vitality
                    }
                }
                description {
                    localization(viewer) {
                        if (cachedPermissions.useVitality) {
                            this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                        } else {
                            this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                        }
                    }
                }
            }
            clickEvent {
                cachedPermissions.useVitality = !cachedPermissions.useVitality
                it.menu.rerender()
            }
        }

        5 to 9 eq {
            icon(ItemStack(Material.EMERALD))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_role_managementModify_roleButtonConfirm
                    }
                }
            }
            clickEvent { menuPageContext ->
                if (role.permission != cachedPermissions) {
                    DB.trans {
                        role.permission = cachedPermissions
                        role.flush()
                    }
                    viewer.send {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("role", role.displayName)
                            }
                        ) {
                            this.guild.guildRoleModify_success
                        }
                    }
                }
                menuPageContext.stack.pop()
            }
        }
    }
}