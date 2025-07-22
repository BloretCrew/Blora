package blora.guild.menu.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.GuildJoinStrategy
import blora.guild.GuildPermissions
import blora.guild.dialog.guildview.guildsettings.guildSettings_modifyDisplayName
import blora.guild.dialog.guildview.guildsettings.guildSettings_modifyIdDialog
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.confirmationMenuLine5
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
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

private val buttons = listOf(
    2 to 2,
    2 to 4,
    2 to 6,
    2 to 8,
    4 to 2,
    4 to 4,
    4 to 6
)

fun guildSettingsMenu(viewer: Player, guild: GuildDao, guilds: MutableList<GuildDao>, permissions: Collection<GuildPermissions>, rerenderGuildViewParent: () -> Unit): SimpleMenuPage {
    var buttonIndex = 0
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_settingsTitle
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

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_ID)) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.PAPER))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsButtonModify_id
                        }
                    }
                    description {
                        newline()
                        localization(viewer) {
                            guild.gid
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    viewer.openDialog(
                        guildSettings_modifyIdDialog(viewer, menuPageContext.menuContext, guild, guilds, permissions, rerenderGuildViewParent)
                    )
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_NAME)) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.PAPER))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsButtonModify_display_name
                        }
                    }
                    description {
                        newline()
                        localization(viewer) {
                            guild.displayName
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    viewer.openDialog(
                        guildSettings_modifyDisplayName(viewer, menuPageContext.menuContext, guild, guilds, permissions, rerenderGuildViewParent)
                    )
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_ICON)) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.PAPER))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsButtonModify_icon
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    menuPageContext.stack.push(
                        guildSettings_modifyIconMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent)
                    )
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_VISIBILITY)) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.PAPER))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsButtonModify_visibility
                        }
                    }
                    description {
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                componentPlaceholder("visibility") {
                                    localization(viewer) {
                                        if (guild.public) {
                                            this.guild.menu.menuGuild_settingsButtonModify_visibilityDescriptionVisible
                                        } else {
                                            this.guild.menu.menuGuild_settingsButtonModify_visibilityDescriptionInvisible
                                        }
                                    }
                                }
                            }
                        ) {
                            this.guild.menu.menuGuild_settingsButtonModify_visibilityDescription
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    DB.trans {
                        guild.public = !guild.public
                        guild.flush()
                    }
                    menuPageContext.menu.rerender()
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_JOIN_STRATEGY)) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.PAPER))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsButtonModify_join_strategy
                        }
                    }
                    description {
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                componentPlaceholder("strategy") {
                                    localization(viewer) {
                                        when (guild.joinStrategy) {
                                            GuildJoinStrategy.DIRECT -> this.guild.guildJoin_strategyDirect
                                            GuildJoinStrategy.INVITE_DIRECT -> this.guild.guildJoin_strategyInvite_direct
                                            GuildJoinStrategy.REQUIRE_REVIEW -> this.guild.guildJoin_strategyRequire_review
                                            GuildJoinStrategy.NOT_ALLOW -> this.guild.guildJoin_strategyNot_allow
                                        }
                                    }
                                }
                            }
                        ) {
                            this.guild.menu.menuGuild_settingsButtonModify_join_strategyDescription
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    menuPageContext.stack.push(guildSettings_modifyJoinStrategyMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent))
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.USE_VITALITY)) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.DIAMOND))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsButtonUpgrade
                        }
                    }
                    description {
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("old", guild.maxMembers.toString())
                                parsedPlaceholder("new", guild.nextLevelMaxMembers.toString())
                            }
                        ) {
                            this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionMembers_limit
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("old", guild.maxClaims.toString())
                                parsedPlaceholder("new", guild.nextLevelMaxClaims.toString())
                            }
                        ) {
                            this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionClaims_limit
                        }
                        newline()
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("cost", guild.upgradeCost.toString())
                            }
                        ) {
                            this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionUpgrade_cost
                        }
                        newline()
                        localization(viewer) {
                            if (guild.upgradeCost <= guild.vitality) {
                                this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionUpgradable
                            } else {
                                this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionVitality_not_enough
                            }
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    if (guild.upgradeCost <= guild.vitality) {
                        DB.trans {
                            guild.vitality -= guild.upgradeCost
                            guild.level += 1
                            guild.flush()
                        }
                        viewer.send {
                            localization(viewer) {
                                this.guild.guildUpgradeSuccess
                            }
                        }
                        menuPageContext.menu.rerender()
                    } else {
                        viewer.send {
                            localization(viewer) {
                                this.guild.guildUpgradeVitality_not_enough
                            }
                        }
                    }
                }
            }
            buttonIndex++
        }

        if (viewer.uniqueId == guild.owner) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.BARRIER))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsButtonDisband_guild
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    menuPageContext.stack.push(confirmationMenuLine5(
                        viewer,
                        component {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("guild", guild.displayName)
                                }
                            ) {
                                this.guild.menu.menuGuild_settingsDisband_guildTitle
                            }
                        }
                    ) {
                        menuPageContext.stack.pop() // goto guild view
                        menuPageContext.stack.pop() // goto guild view parent
                        rerenderGuildViewParent()
                        DB.disbandGuild(guild)
                        guilds.remove(guild)
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("guild_id", guild.gid)
                                    parsedPlaceholder("guild_name", guild.displayName)
                                }
                            ) {
                                this.guild.guildDisbandSuccess
                            }
                        }
                    })
                }
            }
        }
    }
}