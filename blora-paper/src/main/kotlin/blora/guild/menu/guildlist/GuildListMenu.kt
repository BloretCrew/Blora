package blora.guild.menu.guildlist

import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.guild.GuildJoinStrategy
import blora.guild.menu.guildview.guildView
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.mapping
import blora.menu.menuPage
import blora.menu.title
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import blora.util.castString
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildListMenuPage(viewer: Player, showBackButton: Boolean, systemFilter: (Player, GuildDao) -> Boolean, userFilter: (Player, GuildDao) -> Boolean = { _, _ -> true }, currentPage: Int = 1): SimpleMenuPage {
    val guilds = BloraPlugin.database
        .listGuilds()
        .filter { systemFilter(viewer, it) }
        .filter { userFilter(viewer, it) } // use for search
        .toMutableList()
    return menuPage {
        title {
            localization(viewer) {
                this.guild.menu.menuGuild_listTitle
            }
        }
        lines(5)

        mapping(
            "#########",
            "#       #",
            "#       #",
            "#       #",
            "#########"
        )
        '#' eq {
            icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
        }

        if (guilds.size <= (currentPage - 1) * 21 - 1)
            return@menuPage

        if (showBackButton) {
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
        }
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
                    it.stack.replace(guildListMenuPage(viewer, showBackButton, systemFilter, userFilter, currentPage - 1))
                }
            }
        }

        if (guilds.size > (currentPage * 21)) {
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
                    it.stack.replace(guildListMenuPage(viewer, showBackButton, systemFilter, userFilter, currentPage + 1))
                }
            }
        }
        guilds.forEachIndexed { index, guild ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(guild.parsedIcon)
                hoverText {
                    title {
                        localization(player = viewer) {
                            "<italic:false><white>" + guild.displayName
                        }
                    }
                    description {
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("guild_level", guild.level.toString())
                            }
                        ) {
                            "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionLevel
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("guild_owner", BloraPlugin.database.getPlayerDisplayName(guild.owner))
                            }
                        ) {
                            "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionOwner
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("guild_create_date", guild.createAt.castString())
                            }
                        ) {
                            "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionCreated_at
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("guild_members", guild.members.size.toString())
                            }
                        ) {
                            "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionMembers
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("guild_vitality", guild.vitality.toString())
                            }
                        ) {
                            "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionVitality
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                componentPlaceholder("guild_join_strategy") {
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
                            "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionJoin_strategy
                        }
                    }
                }
                clickEvent { clickContext ->
                    if (clickContext.clickType.isLeftClick) {
                        clickContext.stack.push(guildView(viewer, guild, guilds) {
                            if (guilds.size <= (currentPage - 1) * 21 - 1) { // this page does no longer exist
                                clickContext.stack.replace(guildListMenuPage(viewer, showBackButton, systemFilter, userFilter, currentPage - 1))
                            } else {
                                clickContext.stack.replace(guildListMenuPage(viewer, showBackButton, systemFilter, userFilter, currentPage))
                            }
                        })
                    }
                }
            }
        }
    }
}