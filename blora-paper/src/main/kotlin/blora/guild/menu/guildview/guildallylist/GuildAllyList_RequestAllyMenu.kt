package blora.guild.menu.guildview.guildallylist

import blora.database.DB
import blora.database.guild.dao.GuildAllyInfoDao
import blora.database.guild.dao.GuildAllyRequestDao
import blora.database.guild.dao.GuildDao
import blora.database.guild.table.GuildAllyInfoTable
import blora.database.guild.table.GuildAllyRequestTable
import blora.extension.format
import blora.extension.localization
import blora.guild.GuildJoinStrategy
import blora.guild.dataprovider.GuildDaoDataProvider
import blora.item.clone
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import blora.plugin.BloraPlugin
import blora.util.castString
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDateTime

fun guildAllyList_requestAllyMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, GuildDaoDataProvider {
        it.gid != guild.gid && !it.allys.contains(guild.gid) && (DB.trans {
            GuildAllyInfoDao.find {
                (
                        (GuildAllyInfoTable.requestGid eq guild.gid) and
                                (GuildAllyInfoTable.receiveGid eq it.gid)
                        ) or (
                        (GuildAllyInfoTable.requestGid eq it.gid) and
                                (GuildAllyInfoTable.receiveGid eq guild.gid)
                        )
            }.empty()
        }) && (DB.trans {
            GuildAllyRequestDao.find {
                (
                        (
                                (GuildAllyRequestTable.requestGid eq guild.gid) and
                                        (GuildAllyRequestTable.receiveGid eq it.gid)
                                ) or (
                                (GuildAllyRequestTable.requestGid eq it.gid) and
                                        (GuildAllyRequestTable.receiveGid eq guild.gid)
                                )) and (GuildAllyRequestTable.finished eq false)
            }.empty()
        })
    }) {
        pageId {
            "guild_${guildId}_allyList_requestAlly"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_ally_listRequest_allyTitle
            }
        }
        showBackButton()
        dataItem { viewContext, allyGuild, dataIndex ->
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
            }
            clickEvent { clickContext ->
                DB.trans {
                    GuildAllyRequestDao.new {
                        this.requestGuildId = guild.gid
                        this.receiveGuildId = allyGuild.gid

                        this.requestOperator = clickContext.viewer.uniqueId
                        this.requestOperatedAt = LocalDateTime.now()
                    }
                }
                clickContext.menu.rerender()
                clickContext.viewer.send {
                    localization(
                        player = clickContext.viewer,
                        tags = {
                            parsedPlaceholder("guild", allyGuild.displayName)
                        }
                    ) {
                        this.guild.guildAllyRequest
                    }
                }
            }
        }
    }
}