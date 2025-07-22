@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildJoinNotifyDao
import blora.database.guild.dao.GuildJoinRequestDao
import blora.database.guild.table.GuildJoinNotifyTable
import blora.database.guild.table.GuildJoinRequestTable.operator
import blora.extension.localization
import blora.guild.GuildJoinSource
import blora.guild.ReviewResult
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.mapping
import blora.menu.menuPage
import blora.menu.title
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text
import java.time.LocalDateTime

fun guildMemberList_joinRequestsMenu(viewer: Player, guild: GuildDao, requests: MutableList<GuildJoinRequestDao>, currentPage: Int = 1): SimpleMenuPage {
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_member_listJoin_requestsTitle
            }
        }

        mapping(
            "#########",
            "#       #",
            "#       #",
            "#       #",
            "#########",
        )

        '#' eq {
            icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
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
        if (requests.size <= (currentPage - 1) * 21 - 1)
            return@menuPage

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
                    it.stack.pop()
                    it.stack.push(guildMemberList_joinRequestsMenu(viewer, guild, requests,  currentPage - 1))
                }
            }
        }

        if (requests.size > (currentPage * 21)) {
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
                    it.stack.pop()
                    it.stack.push(guildMemberList_joinRequestsMenu(viewer, guild, requests,  currentPage + 1))
                }
            }
        }

        requests.forEachIndexed { index, request ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(ItemStack(Material.PLAYER_HEAD).apply { this.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(
                    Bukkit.getOfflinePlayer(request.player).playerProfile)) })
                hoverText {
                    title {
                        text { DB.getPlayerDisplayName(request.player) }
                    }
                    description {
                        newline()
                        localization(viewer) {
                            this.guild.menu.menuGuild_member_listJoin_requestsDescriptionAccpet
                        }
                        newline()
                        localization(viewer) {
                            this.guild.menu.menuGuild_member_listJoin_requestsDescriptionReject
                        }
                    }
                }
                clickEvent {
                    if (!it.clickType.isLeftClick && !it.clickType.isRightClick)
                        return@clickEvent
                    DB.trans {
                        request.finished = true
                        request.result = it.clickType.isLeftClick
                        request.operatedAt = LocalDateTime.now()
                        request.operator = viewer.uniqueId
                        request.flush()
                    }
                    requests.remove(request)
                    if (requests.size <= (currentPage - 1) * 21 - 1) { // this page does no longer exist
                        it.stack.replace(guildMemberList_joinRequestsMenu(viewer, guild, requests, currentPage - 1))
                    } else {
                        it.stack.replace(guildMemberList_joinRequestsMenu(viewer, guild, requests, currentPage))
                    }
                    if (it.clickType.isLeftClick) {
                        viewer.send {
                            localization(
                                player = viewer,
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
                                reviewer = viewer.uniqueId,
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
                    } else if (it.clickType.isRightClick) {
                        viewer.send {
                            localization(
                                player = viewer,
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
}