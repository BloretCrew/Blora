package blora.guild.menu.guildlist

import blora.database.guild.dao.GuildDao
import blora.extension.format
import blora.extension.localization
import blora.guild.GuildJoinStrategy
import blora.guild.dataprovider.GuildDaoDataProvider
import blora.guild.menu.guildview.guildViewMenu
import blora.item.clone
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.dataItem
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.pageableMenuPage
import blora.menu.v2.page.builder.showBackButton
import blora.menu.v2.page.builder.title
import blora.plugin.BloraPlugin
import blora.util.castString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.raw

fun guildListMenu(
    menu: Menu,
    pageId: String,
    title: Component,
    systemFilter: (Player, GuildDao) -> Boolean,
    userFilter: (Player, GuildDao) -> Boolean = { _, _ -> true }
): MenuPage<*, *> {
    return pageableMenuPage(menu, GuildDaoDataProvider {
        systemFilter(menu.viewer, it) && userFilter(
            menu.viewer,
            it
        )
    }) {
        pageId {
            pageId
        }
        title {
            raw { title }
        }
        showBackButton()
        dataItem { viewContext, guild ->
            icon { clone { guild.parsedIcon } }
            name {
                localization(menu.viewer) {
                    "<italic:false><white>" + guild.displayName
                }
            }
            description {
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_level", guild.level.toString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionLevel
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_owner", BloraPlugin.database.getPlayerDisplayName(guild.owner))
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionOwner
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_create_date", guild.createAt.castString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionCreated_at
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_members", guild.members.size.toString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionMembers
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_vitality", guild.vitality.format(2))
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionVitality
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        componentPlaceholder("guild_join_strategy") {
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
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionJoin_strategy
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.push {
                    guildViewMenu(menu, guild)
                }
            }
        }
    }
}