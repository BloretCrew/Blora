package blora.guild.menu.guildview.guildrolemanagement

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildRoleDao
import blora.extension.localization
import blora.guild.dialog.guildview.guildsettings.guildRoleManagement_modifyRole_modifyNameDialog
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.LimitedDynamicMenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.limitedDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildRoleManagement_modifyRoleMenu(menu: Menu, guild: GuildDao, role: GuildRoleDao): LimitedDynamicMenuPage {
    val guildId = guild.id
    val roleId = role.id
    val cachedPermissions = role.permission.clone()
    return limitedDynamicMenuPage(menu) {
        pageId {
            "guild_${guildId}_roleManagement_modifyRole_${roleId}"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                    parsedPlaceholder("role", role.displayName)
                }
            ) {
                this.guild.menu.menuGuild_role_managementModify_roleTitle
            }
        }
        backButton()
        1 to 5 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_role_managementModify_roleButtonModify_name
                }
            }
            clickEvent { clickContext ->
                guildRoleManagement_modifyRole_modifyNameDialog(clickContext.viewer, guild, role)
            }
        }
        3 to 2 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionModify_guild_name
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.modifyGuildName) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.modifyGuildName = !cachedPermissions.modifyGuildName
                it.menu.rerender()
            }
        }
        3 to 3 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionModify_guild_icon
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.modifyGuildIcon) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.modifyGuildIcon = !cachedPermissions.modifyGuildIcon
                it.menu.rerender()
            }
        }
        3 to 4 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionModify_guild_visibility
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.modifyGuildVisibility) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.modifyGuildVisibility = !cachedPermissions.modifyGuildVisibility
                it.menu.rerender()
            }
        }
        3 to 5 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionModify_guild_join_strategy
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.modifyGuildJoinStrategy) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.modifyGuildJoinStrategy = !cachedPermissions.modifyGuildJoinStrategy
                it.menu.rerender()
            }
        }
        3 to 6 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionKick_player
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.kickPlayer) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.kickPlayer = !cachedPermissions.kickPlayer
                it.menu.rerender()
            }
        }
        3 to 7 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionReview_player
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.reviewPlayer) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.reviewPlayer = !cachedPermissions.reviewPlayer
                it.menu.rerender()
            }
        }
        3 to 8 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionInvite_player
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.invitePlayer) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.invitePlayer = !cachedPermissions.invitePlayer
                it.menu.rerender()
            }
        }
        4 to 2 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionRequest_ally
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.requestAlly) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.requestAlly = !cachedPermissions.requestAlly
                it.menu.rerender()
            }
        }
        4 to 3 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionReview_ally
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.reviewAlly) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.reviewAlly = !cachedPermissions.reviewAlly
                it.menu.rerender()
            }
        }
        4 to 4 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionStop_ally
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.stopAlly) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.stopAlly = !cachedPermissions.stopAlly
                it.menu.rerender()
            }
        }
        4 to 5 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionManage_invitation_code
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.manageInvitationCode) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.manageInvitationCode = !cachedPermissions.manageInvitationCode
                it.menu.rerender()
            }
        }
        4 to 6 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionStore_bank
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.storeBank) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.storeBank = !cachedPermissions.storeBank
                it.menu.rerender()
            }
        }
        4 to 7 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionWithdraw_bank
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.withdrawBank) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.withdrawBank = !cachedPermissions.withdrawBank
                it.menu.rerender()
            }
        }
        4 to 8 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildRole_permissionUse_vitality
                }
            }
            description {
                localization(menu.viewer) {
                    if (cachedPermissions.useVitality) {
                        this.guild.menu.menuGuild_role_managementModify_roleItemAllow
                    } else {
                        this.guild.menu.menuGuild_role_managementModify_roleItemDeny
                    }
                }
            }
            clickEvent {
                cachedPermissions.useVitality = !cachedPermissions.useVitality
                it.menu.rerender()
            }
        }

        5 to 9 eq {
            icon { material { Material.EMERALD } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_role_managementModify_roleButtonConfirm
                }
            }
            clickEvent { clickContext ->
                DB.trans {
                    guild.refresh()
                }
                if (guild.owner == clickContext.viewer.uniqueId && role.permission != cachedPermissions) {
                    DB.trans {
                        role.permission = cachedPermissions
                        role.flush()
                    }
                    clickContext.viewer.send {
                        localization(
                            player = clickContext.viewer,
                            tags = {
                                parsedPlaceholder("role", role.displayName)
                            }
                        ) {
                            this.guild.guildRoleModify_success
                        }
                    }
                }
                clickContext.stack.pop()
            }
        }
    }
}