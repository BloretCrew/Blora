package blora.guild.menu.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.GuildJoinStrategy
import blora.guild.GuildPermissions
import blora.guild.dialog.guildview.guildsettings.guildSettings_modifyDisplayNameDialog
import blora.guild.dialog.guildview.guildsettings.guildSettings_modifyIdDialog
import blora.item.material
import blora.menu.line5_confirmrationMenu
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.completeDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import org.bukkit.Material
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

fun guildSettingsMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return completeDynamicMenuPage(menu) {
        DB.trans {
            guild.refresh()
        } // ensure data updated for security
        val isMember = guild.members.contains(menu.viewer.uniqueId)
        val permissions = guild.getPlayerPermissions(menu.viewer.uniqueId, isMember)

        pageId {
            "guild_${guildId}_settings"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_settingsTitle
            }
        }

        backButton()

        var buttonIndex = 0

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_ID)) {
            buttons[buttonIndex] eq {
                icon { material { Material.PAPER } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsButtonModify_id
                    }
                }
                description {
                    newline()
                    localization(menu.viewer) {
                        guild.gid
                    }
                }
                clickEvent { clickContext ->
                    clickContext.viewer.openDialog(
                        guildSettings_modifyIdDialog(menu.viewer, guild, {
                            clickContext.menu.rerender()
                        })
                    )
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_NAME)) {
            buttons[buttonIndex] eq {
                icon { material { Material.PAPER } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsButtonModify_display_name
                    }
                }
                description {
                    newline()
                    localization(menu.viewer) {
                        guild.displayName
                    }
                }
                clickEvent { clickContext ->
                    clickContext.viewer.openDialog(
                        guildSettings_modifyDisplayNameDialog(menu.viewer, guild) {
                            clickContext.menu.rerender()
                        }
                    )
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_ICON)) {
            buttons[buttonIndex] eq {
                icon { material { Material.PAPER } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsButtonModify_icon
                    }
                }
                clickEvent { clickContext ->
                    clickContext.stack.push {
                        guildSettings_modifyIconMenu(menu, guild)
                    }
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_VISIBILITY)) {
            buttons[buttonIndex] eq {
                icon { material { Material.PAPER } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsButtonModify_visibility
                    }
                }
                description {
                    newline()
                    localization(
                        player = menu.viewer,
                        tags = {
                            componentPlaceholder("visibility") {
                                localization(menu.viewer) {
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
                clickEvent { clickContext ->
                    DB.trans {
                        guild.refresh()
                    }
                    val isMember = guild.members.contains(menu.viewer.uniqueId)
                    val permissions = guild.getPlayerPermissions(menu.viewer.uniqueId, isMember)
                    if (permissions.contains(GuildPermissions.MODIFY_GUILD_VISIBILITY)) {
                        DB.trans {
                            guild.public = !guild.public
                            guild.flush()
                        }
                        clickContext.menu.rerender()
                    }
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.MODIFY_GUILD_JOIN_STRATEGY)) {
            buttons[buttonIndex] eq {
                icon { material { Material.PAPER } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsButtonModify_join_strategy
                    }
                }
                description {
                    newline()
                    localization(
                        player = menu.viewer,
                        tags = {
                            componentPlaceholder("strategy") {
                                localization(menu.viewer) {
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
                clickEvent { clickContext ->
                    clickContext.stack.push {
                        guildSettings_modifyJoinStrategyMenu(menu, guild)
                    }
                }
            }
            buttonIndex++
        }

        if (permissions.contains(GuildPermissions.USE_VITALITY)) {
            buttons[buttonIndex] eq {
                icon { material { Material.DIAMOND } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsButtonUpgrade
                    }
                }
                description {
                    newline()
                    localization(
                        player = menu.viewer,
                        tags = {
                            parsedPlaceholder("old", guild.maxMembers.toString())
                            parsedPlaceholder("new", guild.nextLevelMaxMembers.toString())
                        }
                    ) {
                        this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionMembers_limit
                    }
                    newline()
                    localization(
                        player = menu.viewer,
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
                        player = menu.viewer,
                        tags = {
                            parsedPlaceholder("cost", guild.upgradeCost.toString())
                        }
                    ) {
                        this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionUpgrade_cost
                    }
                    newline()
                    localization(menu.viewer) {
                        if (guild.upgradeCost <= guild.vitality) {
                            this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionUpgradable
                        } else {
                            this.guild.menu.menuGuild_settingsButtonUpgradeDescriptionVitality_not_enough
                        }
                    }
                }
                clickEvent { clickContext ->
                    DB.trans {
                        guild.refresh()
                    }
                    val isMember = guild.members.contains(menu.viewer.uniqueId)
                    val permissions = guild.getPlayerPermissions(menu.viewer.uniqueId, isMember)
                    if (permissions.contains(GuildPermissions.USE_VITALITY)) {
                        if (guild.upgradeCost <= guild.vitality) {
                            DB.trans {
                                guild.vitality -= guild.upgradeCost
                                guild.level += 1
                                guild.flush()
                            }
                            clickContext.viewer.send {
                                localization(clickContext.viewer) {
                                    this.guild.guildUpgradeSuccess
                                }
                            }
                        } else {
                            clickContext.viewer.send {
                                localization(clickContext.viewer) {
                                    this.guild.guildUpgradeVitality_not_enough
                                }
                            }
                        }
                    }
                    clickContext.menu.rerender()
                }
            }
            buttonIndex++
        }

        if (menu.viewer.uniqueId == guild.owner) {
            buttons[buttonIndex] eq {
                icon { material { Material.BARRIER } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsButtonDisband_guild
                    }
                }
                clickEvent { clickContext ->
                    clickContext.stack.push(
                        line5_confirmrationMenu(
                            clickContext.menu,
                            component {
                                localization(
                                    player = clickContext.viewer,
                                    tags = {
                                        parsedPlaceholder("guild", guild.displayName)
                                    }
                                ) {
                                    this.guild.menu.menuGuild_settingsDisband_guildTitle
                                }
                            }
                        ) {
                            DB.trans {
                                guild.refresh()
                            }
                            if (guild.owner == clickContext.viewer.uniqueId) {
                                clickContext.stack.pop() // goto guild view
                                clickContext.stack.pop() // goto guild view parent
                                DB.disbandGuild(guild)
                                clickContext.viewer.send {
                                    localization(
                                        player = clickContext.viewer,
                                        tags = {
                                            parsedPlaceholder("guild_id", guild.gid)
                                            parsedPlaceholder("guild_name", guild.displayName)
                                        }
                                    ) {
                                        this.guild.guildDisbandSuccess
                                    }
                                }
                            }
                        })
                }
            }
        }
    }
}