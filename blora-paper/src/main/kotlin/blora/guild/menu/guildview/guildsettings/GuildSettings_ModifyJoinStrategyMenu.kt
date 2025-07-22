package blora.guild.menu.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.guild.GuildJoinStrategy
import blora.guild.GuildPermissions
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
import plutoproject.adventurekt.text.newline

fun guildSettings_modifyJoinStrategyMenu(viewer: Player, guild: GuildDao, guilds: MutableList<GuildDao>, permissions: Collection<GuildPermissions>, rerenderGuildViewParent: () -> Unit, cachedJoinStrategy: GuildJoinStrategy = guild.joinStrategy): SimpleMenuPage {
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {

                }
            ) {
                this.guild.menu.menuGuild_settingsModify_join_strategyTitle
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

        3 to 2 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildJoin_strategyDirect
                    }
                }
                if (cachedJoinStrategy == GuildJoinStrategy.DIRECT) {
                    description {
                        newline()
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsModify_join_strategySelected
                        }
                    }
                }
            }
            clickEvent {
                it.stack.replace(guildSettings_modifyJoinStrategyMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent, GuildJoinStrategy.DIRECT))
            }
        }

        3 to 4 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildJoin_strategyInvite_direct
                    }
                }
                if (cachedJoinStrategy == GuildJoinStrategy.INVITE_DIRECT) {
                    description {
                        newline()
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsModify_join_strategySelected
                        }
                    }
                }
            }
            clickEvent {
                it.stack.replace(guildSettings_modifyJoinStrategyMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent, GuildJoinStrategy.INVITE_DIRECT))
            }
        }

        3 to 6 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildJoin_strategyRequire_review
                    }
                }
                if (cachedJoinStrategy == GuildJoinStrategy.REQUIRE_REVIEW) {
                    description {
                        newline()
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsModify_join_strategySelected
                        }
                    }
                }
            }
            clickEvent {
                it.stack.replace(guildSettings_modifyJoinStrategyMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent, GuildJoinStrategy.REQUIRE_REVIEW))
            }
        }

        3 to 8 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.guildJoin_strategyNot_allow
                    }
                }
                if (cachedJoinStrategy == GuildJoinStrategy.NOT_ALLOW) {
                    description {
                        newline()
                        localization(viewer) {
                            this.guild.menu.menuGuild_settingsModify_join_strategySelected
                        }
                    }
                }
            }
            clickEvent {
                it.stack.replace(guildSettings_modifyJoinStrategyMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent, GuildJoinStrategy.NOT_ALLOW))
            }
        }

        5 to 9 eq {
            icon(ItemStack(Material.EMERALD))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_settingsModify_join_strategyButtonConfirm
                    }
                }
            }
            clickEvent { menuPageContext ->
                if (guild.joinStrategy != cachedJoinStrategy) {
                    DB.trans {
                        guild.joinStrategy = cachedJoinStrategy
                        guild.flush()
                    }
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildUpdateJoin_strategy
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