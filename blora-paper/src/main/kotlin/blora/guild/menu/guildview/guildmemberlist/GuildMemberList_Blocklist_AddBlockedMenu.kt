@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildJoinNotifyDao
import blora.database.guild.table.GuildInvitationTable
import blora.database.guild.table.GuildJoinNotifyTable
import blora.database.guild.table.GuildJoinRequestTable
import blora.database.guild.table.GuildMemberInfoTable
import blora.extension.localization
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text

fun guildMemberList_blocklist_addBlockedMenu(viewer: Player, guild: GuildDao, requestCurrentPage: Int = 1): SimpleMenuPage {
    val blockable = Bukkit.getOnlinePlayers()
        .filter { it != viewer }
        .filter { !guild.members.contains(it.uniqueId) }
        .filter { !guild.blocklist.contains(it.uniqueId) }
        .filter { it.isOnline }
        .filter { it.uniqueId != guild.owner } // 能把会长拉黑那真是倒反天罡了
        .toMutableList()

    var currentPage = requestCurrentPage

    while (blockable.size <= (currentPage - 1) * 21 - 1 && currentPage > 1)
        currentPage -= 1

    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_member_listBlocklistAdd_blockedTitle
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
                    it.stack.replace(guildMemberList_blocklist_addBlockedMenu(viewer, guild, currentPage - 1))
                }
            }
        }

        if (blockable.size > (currentPage * 21)) {
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
                    it.stack.replace(guildMemberList_blocklist_addBlockedMenu(viewer, guild, currentPage + 1))
                }
            }
        }

        blockable.forEachIndexed { index, blockablePlayer ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(ItemStack(Material.PLAYER_HEAD).apply {
                    this.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(blockablePlayer.playerProfile))
                })
                hoverText {
                    title {
                        text { blockablePlayer.name }
                    }
                }
                clickEvent {
                    val roles = DB.listRolesForPlayer(blockablePlayer.uniqueId, guild.gid)
                    DB.trans {
                        for (role in roles) {
                            role.ownedMembers = role.ownedMembers.toMutableList().apply { remove(blockablePlayer.uniqueId) }.toList()
                            role.flush()
                        }
                        GuildInvitationTable.deleteWhere {
                            GuildInvitationTable.invitee eq blockablePlayer.uniqueId and
                                    (GuildInvitationTable.gid eq guild.gid)
                        }
                        GuildJoinNotifyTable.deleteWhere {
                            GuildJoinNotifyTable.player eq blockablePlayer.uniqueId and
                                    (GuildJoinNotifyTable.gid eq guild.gid)
                        }
                        GuildMemberInfoTable.deleteWhere {
                            GuildMemberInfoTable.player eq blockablePlayer.uniqueId and
                                    (GuildMemberInfoTable.gid eq guild.gid)
                        }
                        GuildJoinRequestTable.deleteWhere {
                            GuildJoinRequestTable.player eq blockablePlayer.uniqueId and
                                    (GuildJoinRequestTable.gid eq guild.gid)
                        }
                        guild.blocklist = guild.blocklist.toMutableList().apply { add(blockablePlayer.uniqueId) }.toList()
                        guild.flush()
                    }
                    // due to only players not in guild can be blocked, so not announce them
                    /*viewer.send {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("guild", guild.displayName)
                                parsedPlaceholder("player", blockablePlayer.name)
                            }
                        ) {
                            this.guild.guildBlocklistAdd
                        }
                    }*/
                    blockablePlayer.send {
                        localization(
                            player = blockablePlayer,
                            tags = {
                                parsedPlaceholder("guild", guild.displayName)
                            }
                        ) {
                            this.guild.guildBlocklistAdded
                        }
                    }
                    it.stack.replace(guildMemberList_blocklist_addBlockedMenu(viewer, guild, currentPage))
                }
            }
        }
    }
}