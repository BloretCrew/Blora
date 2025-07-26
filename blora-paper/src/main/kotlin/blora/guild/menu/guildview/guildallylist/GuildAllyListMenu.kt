package blora.guild.menu.guildview.guildallylist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.format
import blora.extension.localization
import blora.guild.GuildJoinStrategy
import blora.guild.dataprovider.AllyGuildDaoDataProvider
import blora.guild.menu.guildview.guildViewMenu
import blora.item.clone
import blora.item.material
import blora.menu.line5_confirmrationMenu
import blora.menu.v2.page.MenuPage
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.builder.dataItem
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.pageableMenuPage
import blora.menu.v2.page.builder.showBackButton
import blora.menu.v2.page.builder.title
import blora.plugin.BloraPlugin
import blora.util.castString
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildAllyListMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, AllyGuildDaoDataProvider(guild)) {
        val permissions = DB.getRolePermissions(menu.viewer.uniqueId, guild.gid)
        pageId {
            "guild_${guildId}_allyList"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_ally_listTitle
            }
        }
        showBackButton()
        if (permissions.requestAlly) {
            5 to 4 eq {
                icon { material { Material.GOLD_INGOT } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_ally_listButtonRequest_ally
                    }
                }
                clickEvent { clickContext ->
                    clickContext.stack.push {
                        guildAllyList_requestAllyMenu(menu, guild)
                    }
                }
            }
        }
        if (permissions.reviewAlly) {
            val requests = DB.listValidAllyRequestsForGuild(guild.gid).toMutableList()
            5 to 5 eq {
                icon { material { Material.COMPASS } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_ally_listButtonAlly_requests
                    }
                }
                description {
                    localization(
                        menu.viewer,
                        tags = {
                            parsedPlaceholder("requests", requests.size.toString())
                        }
                    ) {
                        this.guild.menu.menuGuild_ally_listButtonAlly_requestsDescription
                    }
                }
                clickEvent { clickContext ->
                    clickContext.stack.push {
                        guildAllyList_allyRequestsMenu(menu, guild)
                    }
                }
            }
        }
        dataItem { viewContext, (allyGuild, allyInfo) ->
            icon { clone { allyGuild.parsedIcon } }
            name {
                localization(menu.viewer) {
                    "<italic:false><white>" + allyGuild.displayName
                }
            }
            description {
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_level", allyGuild.level.toString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionLevel
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_owner", BloraPlugin.database.getPlayerDisplayName(allyGuild.owner))
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionOwner
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_create_date", allyGuild.createAt.castString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionCreated_at
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_members", allyGuild.members.size.toString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionMembers
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_vitality", allyGuild.vitality.format(2))
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
                                when (allyGuild.joinStrategy) {
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
                newline()
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_ally_date", allyInfo.receiveOperatedAt.castString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionAlly_at
                }
                if (permissions.stopAlly) {
                    newline()
                    newline()
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_ally_listItemDescriptionRight
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isLeftClick) {
                    clickContext.stack.push {
                        guildViewMenu(menu, guild)
                    }
                } else if (clickContext.click.isRightClick && permissions.stopAlly) {
                    clickContext.stack.push {
                        line5_confirmrationMenu(
                            menu,
                            component {
                                localization(
                                    player = clickContext.viewer,
                                    tags = {
                                        parsedPlaceholder("guild", allyGuild.displayName)
                                    }
                                ) {
                                    this.guild.menu.menuGuild_ally_listStop_allyTitle
                                }
                            }
                        ) {
                            DB.trans {
                                allyInfo.delete()
                                guild.allys = guild.allys.toMutableList().apply {
                                    remove(allyGuild.gid)
                                }.toList()
                                allyGuild.allys = allyGuild.allys.toMutableList().apply {
                                    remove(guild.gid)
                                }.toList()
                                guild.refresh()
                                allyGuild.refresh()
                            }
                            clickContext.viewer.send {
                                localization(
                                    player = clickContext.viewer,
                                    tags = {
                                        parsedPlaceholder("guild", allyGuild.displayName)
                                    }
                                ) {
                                    this.guild.guildAllyStop
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}