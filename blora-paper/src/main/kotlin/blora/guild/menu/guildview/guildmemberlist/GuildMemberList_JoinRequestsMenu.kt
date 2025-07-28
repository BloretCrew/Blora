@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildJoinNotifyDao
import blora.extension.localization
import blora.extension.resolvableProfile
import blora.guild.ReviewResult
import blora.guild.dataprovider.ValidGuildJoinRequestDaoDataProvider
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Bukkit
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text
import java.time.LocalDateTime

fun guildMemberList_joinRequestsMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, ValidGuildJoinRequestDaoDataProvider(guild.gid)) {
        pageId {
            "guild_${guildId}_memberList_joinRequests"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_member_listJoin_requestsTitle
            }
        }
        showBackButton()
        dataItem { viewContext, request, dataIndex ->
            icon {
                material { Material.PLAYER_HEAD }
                DataComponentTypes.PROFILE eq request.player.resolvableProfile()
            }
            title {
                text { DB.getPlayerDisplayName(request.player) }
            }
            description {
                newline()
                localization(viewContext.viewer) {
                    this.guild.menu.menuGuild_member_listJoin_requestsDescriptionAccpet
                }
                newline()
                localization(viewContext.viewer) {
                    this.guild.menu.menuGuild_member_listJoin_requestsDescriptionReject
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isLeftClick && guild.members.size >= guild.maxMembers) {
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.guild.guildJoin_invite_reviewMembers_limit
                        }
                    }
                    return@clickEvent
                }
                if (!clickContext.click.isLeftClick && !clickContext.click.isRightClick)
                    return@clickEvent
                if (!DB.getRolePermissions(viewContext.viewer.uniqueId, guild.gid).reviewPlayer) {
                    clickContext.stack.pop()
                    return@clickEvent
                }
                DB.trans {
                    request.finished = true
                    request.result = clickContext.click.isLeftClick
                    request.operatedAt = LocalDateTime.now()
                    request.operator = clickContext.viewer.uniqueId
                    request.flush()
                }
                clickContext.menu.rerender()
                if (clickContext.click.isLeftClick) {
                    clickContext.viewer.send {
                        localization(
                            player = clickContext.viewer,
                            tags = {
                                parsedPlaceholder("guild", guild.displayName)
                                parsedPlaceholder("player", DB.getPlayerDisplayName(request.player))
                            }
                        ) {
                            this.guild.guildJoin_requestAccept
                        }
                    }
                    DB.guildJoinInRequest(
                        guild,
                        request.player,
                        request.parsedJoinSource,
                        request,
                        ReviewResult(
                            reviewer = clickContext.viewer.uniqueId,
                            reviewedAt = LocalDateTime.now()
                        )
                    )
                    val player = Bukkit.getPlayer(request.player)
                    if (player != null && player.isOnline) {
                        player.send {
                            localization(
                                player = player,
                                tags = {
                                    parsedPlaceholder("guild", guild.displayName)
                                }
                            ) {
                                this.guild.guildJoin_requestAccepted
                            }
                        }
                    } else {
                        DB.trans {
                            GuildJoinNotifyDao.new {
                                this.guildId = guild.gid
                                this.guildName = guild.displayName
                                this.player = request.player
                                this.result = true
                            }
                        }
                    }
                } else if (clickContext.click.isRightClick) {
                    clickContext.viewer.send {
                        localization(
                            player = clickContext.viewer,
                            tags = {
                                parsedPlaceholder("guild", guild.displayName)
                                parsedPlaceholder("player", DB.getPlayerDisplayName(request.player))
                            }
                        ) {
                            this.guild.guildJoin_requestReject
                        }
                    }
                    val player = Bukkit.getPlayer(request.player)
                    if (player != null && player.isOnline) {
                        player.send {
                            localization(
                                player = player,
                                tags = {
                                    parsedPlaceholder("guild", guild.displayName)
                                }
                            ) {
                                this.guild.guildJoin_requestRejected
                            }
                        }
                    } else {
                        DB.trans {
                            GuildJoinNotifyDao.new {
                                this.guildId = guild.gid
                                this.guildName = guild.displayName
                                this.player = request.player
                                this.result = false
                            }
                        }
                    }
                }
            }
        }
    }
}