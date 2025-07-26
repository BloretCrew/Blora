package blora.guild.menu.guildview.guildallylist

import blora.database.DB
import blora.database.guild.dao.GuildAllyInfoDao
import blora.database.guild.dao.GuildAllyRequestDao
import blora.database.guild.dao.GuildDao
import blora.extension.format
import blora.extension.localization
import blora.guild.GuildJoinStrategy
import blora.guild.dataprovider.GuildAllyRequestDaoProvider
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
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDateTime

fun guildAllyList_allyRequestsMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, GuildAllyRequestDaoProvider(guild)) {
        pageId {
            "guild_${guildId}_allyList_allyRequests"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_ally_listAlly_requestsTitle
            }
        }
        showBackButton()
        dataItem { viewContext, (request, allyGuild) ->
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
                        parsedPlaceholder("guild_ally_request_date", request.requestOperatedAt.castString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionAlly_request_at
                }
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_ally_requester", DB.getPlayerDisplayName(request.requestOperator))
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionAlly_requester
                }
                newline()
                newline()
                localization(menu.viewer) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_ally_listAlly_requestsItemDescriptionLeft
                }
                newline()
                localization(menu.viewer) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_ally_listAlly_requestsItemDescriptionRight
                }
            }
            clickEvent { clickContext ->
                if (!(clickContext.click.isLeftClick || clickContext.click.isRightClick))
                    return@clickEvent
                DB.trans {
                    request.finished = true
                    request.result = clickContext.click.isLeftClick
                    request.receiveOperator = clickContext.viewer.uniqueId
                    request.receiveOperatedAt = LocalDateTime.now()
                    request.flush()
                }
                if (clickContext.click.isLeftClick) {
                    DB.trans {
                        guild.allys = guild.allys.toMutableList().apply {
                            add(allyGuild.gid)
                        }.toList()
                        allyGuild.allys = allyGuild.allys.toMutableList().apply {
                            add(guild.gid)
                        }.toList()
                        guild.flush()
                        allyGuild.flush()
                        GuildAllyInfoDao.new {
                            this.requestGuildId = allyGuild.gid
                            this.requestOperator = request.requestOperator
                            this.requestOperatedAt = request.requestOperatedAt

                            this.receiveGuildId = guild.gid
                            this.receiveOperator = clickContext.viewer.uniqueId
                            this.receiveOperatedAt = LocalDateTime.now()
                        }
                    }
                    clickContext.viewer.send {
                        localization(
                            player = clickContext.viewer,
                            tags = {
                                parsedPlaceholder("guild", allyGuild.displayName)
                            }
                        ) {
                            this.guild.guildAllyAccept
                        }
                    }
                } else {
                    clickContext.viewer.send {
                        localization(
                            player = clickContext.viewer,
                            tags = {
                                parsedPlaceholder("guild", allyGuild.displayName)
                            }
                        ) {
                            this.guild.guildAllyReject
                        }
                    }
                }
            }
        }
    }
}