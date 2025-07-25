package blora.guild.menu.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.guild.GuildJoinStrategy
import blora.guild.GuildPermissions
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.limitedDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline

fun guildSettings_modifyJoinStrategyMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    var cachedJoinStrategy = guild.joinStrategy
    return limitedDynamicMenuPage(menu) {
        pageId {
            "guild_${guildId}_settings_modifyJoinStrategy"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {

                }
            ) {
                this.guild.menu.menuGuild_settingsModify_join_strategyTitle
            }
        }

        backButton()

        3 to 2 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildJoin_strategyDirect
                }
            }
            if (cachedJoinStrategy == GuildJoinStrategy.DIRECT) {
                description {
                    newline()
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsModify_join_strategySelected
                    }
                }
            }
            clickEvent { clickContext ->
                cachedJoinStrategy = GuildJoinStrategy.DIRECT
                clickContext.menu.rerender()
            }
        }

        3 to 4 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildJoin_strategyInvite_direct
                }
            }
            if (cachedJoinStrategy == GuildJoinStrategy.INVITE_DIRECT) {
                description {
                    newline()
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsModify_join_strategySelected
                    }
                }
            }
            clickEvent { clickContext ->
                cachedJoinStrategy = GuildJoinStrategy.INVITE_DIRECT
                clickContext.menu.rerender()
            }
        }

        3 to 6 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildJoin_strategyRequire_review
                }
            }
            if (cachedJoinStrategy == GuildJoinStrategy.REQUIRE_REVIEW) {
                description {
                    newline()
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsModify_join_strategySelected
                    }
                }
            }
            clickEvent { clickContext ->
                cachedJoinStrategy = GuildJoinStrategy.REQUIRE_REVIEW
                clickContext.menu.rerender()
            }
        }

        3 to 8 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.guildJoin_strategyNot_allow
                }
            }
            if (cachedJoinStrategy == GuildJoinStrategy.NOT_ALLOW) {
                description {
                    newline()
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_settingsModify_join_strategySelected
                    }
                }
            }
            clickEvent { clickContext ->
                cachedJoinStrategy = GuildJoinStrategy.NOT_ALLOW
                clickContext.menu.rerender()
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
                if (permissions.contains(GuildPermissions.MODIFY_GUILD_JOIN_STRATEGY)) {
                    if (guild.joinStrategy != cachedJoinStrategy) {
                        DB.trans {
                            guild.joinStrategy = cachedJoinStrategy
                            guild.flush()
                        }
                        clickContext.viewer.send {
                            localization(clickContext.viewer) {
                                this.guild.guildUpdateJoin_strategy
                            }
                        }
                    }
                }
                clickContext.stack.pop()
            }
        }
    }
}