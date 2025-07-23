@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildBlocklistDao
import blora.database.guild.dao.GuildDao
import blora.database.guild.table.GuildInvitationTable
import blora.database.guild.table.GuildJoinNotifyTable
import blora.database.guild.table.GuildJoinRequestTable
import blora.database.guild.table.GuildMemberInfoTable
import blora.extension.localization
import blora.extension.resolvableProfile
import blora.guild.dataprovider.OnlinePlayerDataProvider
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.dataItem
import blora.menu.v2.page.builder.pageableMenuPage
import blora.menu.v2.page.builder.showBackButton
import blora.menu.v2.page.builder.title
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text
import java.time.LocalDateTime

fun guildMemberList_blocklist_addBlockedMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    return pageableMenuPage(
        menu,
        OnlinePlayerDataProvider { player ->
            (player != menu.viewer) &&
                    (!guild.members.contains(player.uniqueId)) &&
                    (!guild.blocklist.contains(player.uniqueId)) &&
                    (player.uniqueId != guild.owner) &&
                    (player.isOnline)
        }
    ) {
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_member_listBlocklistAdd_blockedTitle
            }
        }
        showBackButton()
        dataItem { viewContext, blockablePlayer ->
            icon {
                material { Material.PLAYER_HEAD }
                DataComponentTypes.PROFILE eq blockablePlayer.resolvableProfile()
            }
            name {
                text { blockablePlayer.name }
            }
            clickEvent { clickContext ->
                if (!DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid).blocklist) {
                    clickContext.stack.pop()
                    clickContext.stack.pop()
                    return@clickEvent
                }
                val roles = DB.listRolesForPlayer(blockablePlayer.uniqueId, guild.gid)
                DB.trans {
                    for (role in roles) {
                        role.ownedMembers =
                            role.ownedMembers.toMutableList().apply { remove(blockablePlayer.uniqueId) }.toList()
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

                    GuildBlocklistDao.new {
                        this.guildId = guild.gid
                        this.player = blockablePlayer.uniqueId
                        this.blockedBy = clickContext.viewer.uniqueId
                        this.blockedAt = LocalDateTime.now()
                    }
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
                clickContext.menu.rerender()
            }
        }
    }
}