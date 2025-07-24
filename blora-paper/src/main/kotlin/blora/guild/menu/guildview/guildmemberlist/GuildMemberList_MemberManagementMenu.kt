@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildKickNotifyDao
import blora.database.guild.dao.GuildMemberInfoDao
import blora.database.guild.table.GuildInvitationTable
import blora.database.guild.table.GuildJoinNotifyTable
import blora.database.guild.table.GuildJoinRequestTable
import blora.database.guild.table.GuildMemberInfoTable
import blora.extension.localization
import blora.extension.resolvableProfile
import blora.item.material
import blora.menu.line5_confirmrationMenu
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.completeDynamicMenuPage
import blora.menu.v2.page.builder.title
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Bukkit
import org.bukkit.Material
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

private val buttons = listOf(
    4 to 2,
    4 to 4,
    4 to 6,
    4 to 8,
)

fun guildMemberList_memberManagementMenu(menu: Menu, guild: GuildDao, member: GuildMemberInfoDao): MenuPage<*, *> {
    return completeDynamicMenuPage(menu) {
        val cachedPlayerName = DB.getPlayerDisplayName(member.player)
        val viewerPriority = DB.getMaxRolePriority(menu.viewer.uniqueId, guild)
        val playerPriority = DB.getMaxRolePriority(member.player, guild)
        val isOwner = menu.viewer.uniqueId == guild.owner
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                    parsedPlaceholder("player", cachedPlayerName)
                }
            ) {
                this.guild.menu.menuGuild_member_listMember_managementTitle
            }
        }
        backButton()

        2 to 5 eq {
            icon {
                material { Material.PLAYER_HEAD }
                DataComponentTypes.PROFILE eq member.player.resolvableProfile()
            }
            name {
                text { cachedPlayerName }
            }
        }

        var buttonIndex = 0

        if (isOwner) {
            buttons[buttonIndex] eq {
                icon { material { Material.NETHER_STAR } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_member_listMember_managementButtonTransfer_owner
                    }
                }
                clickEvent { clickContext ->
                    DB.trans {
                        guild.refresh()
                    }
                    if (guild.owner != clickContext.viewer.uniqueId) {
                        clickContext.menu.rerender()
                        return@clickEvent
                    }
                    val viewer = clickContext.viewer
                    if (CONF.guild.ownerTransferCooldownDays > 0) {
                        if (guild.lastOwnerTransferDate.plusDays(CONF.guild.ownerTransferCooldownDays.toLong()) >= LocalDate.now()) {
                            viewer.send {
                                localization(
                                    player = viewer,
                                    tags = {
                                        parsedPlaceholder(
                                            "days",
                                            abs(
                                                ChronoUnit.DAYS.between(
                                                    guild.lastOwnerTransferDate.plusDays(CONF.guild.ownerTransferCooldownDays.toLong()),
                                                    LocalDate.now()
                                                )
                                            ).toString()
                                        )
                                    }
                                ) {
                                    this.guild.guildTransfer_ownerCooldown
                                }
                            }
                            return@clickEvent
                        }
                    }
                    val adminRole = DB.getAdminRole(guild)
                    DB.trans {
                        guild.owner = member.player
                        guild.lastOwnerTransferDate = LocalDate.now()
                        guild.flush()

                        adminRole.ownedMembers = adminRole.ownedMembers.toMutableList().apply {
                            this.remove(viewer.uniqueId)
                        }.toList()
                        adminRole.flush()
                    }
                    Bukkit.getOnlinePlayers()
                        .filter { guild.members.contains(it.uniqueId) }
                        .forEach { player ->
                            player.send {
                                localization(
                                    player = viewer,
                                    tags = {
                                        parsedPlaceholder("guild", guild.displayName)
                                        parsedPlaceholder("old_owner", viewer.name)
                                        parsedPlaceholder("new_owner", cachedPlayerName)
                                    }
                                ) {
                                    this.guild.guildTransfer_ownerAnnouncement
                                }
                            }
                        }
                    clickContext.stack.pop() // goto member list
                    clickContext.stack.pop() // goto guild view
                }
            }
            buttonIndex++
            buttons[buttonIndex] eq {
                icon { material { Material.BOOKSHELF } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_member_listMember_managementButtonModify_roles
                    }
                }
                clickEvent { clickContext ->
                    clickContext.stack.push {
                        guildMemberList_memberManagement_roleManagementMenu(menu, guild, member)
                    }
                }
            }
            buttonIndex++
        }

        if (member.player != menu.viewer.uniqueId && viewerPriority > playerPriority) {
            buttons[buttonIndex] eq {
                icon { material { Material.BARRIER } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_member_listMember_managementButtonKick
                    }
                }
                clickEvent { clickContext ->
                    if (!DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid).kickPlayer) {
                        clickContext.menu.rerender()
                        return@clickEvent
                    }
                    clickContext.stack.push {
                        line5_confirmrationMenu(
                            menu,
                            component {
                                localization(
                                    player = clickContext.viewer,
                                    tags = {
                                        parsedPlaceholder("player", cachedPlayerName)
                                    }
                                ) {
                                    this.guild.menu.menuGuild_member_listMember_managementKickTitle
                                }
                            }
                        ) {
                            if (!DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid).kickPlayer) {
                                clickContext.menu.rerender()
                                return@line5_confirmrationMenu
                            }
                            val roles = DB.listRolesForPlayer(member.player, guild.gid)
                            DB.trans {
                                for (role in roles) {
                                    role.ownedMembers =
                                        role.ownedMembers.toMutableList().apply { remove(member.player) }.toList()
                                    role.flush()
                                }
                                GuildInvitationTable.deleteWhere {
                                    GuildInvitationTable.invitee eq member.player and
                                            (GuildInvitationTable.gid eq guild.gid)
                                }
                                GuildJoinNotifyTable.deleteWhere {
                                    GuildJoinNotifyTable.player eq member.player and
                                            (GuildJoinNotifyTable.gid eq guild.gid)
                                }
                                GuildMemberInfoTable.deleteWhere {
                                    GuildMemberInfoTable.player eq member.player and
                                            (GuildMemberInfoTable.gid eq guild.gid)
                                }
                                GuildJoinRequestTable.deleteWhere {
                                    GuildJoinRequestTable.player eq member.player and
                                            (GuildJoinRequestTable.gid eq guild.gid)
                                }
                                guild.members = guild.members.toMutableList().apply { remove(member.player) }.toList()
                                guild.flush()
                            }
                            val player = Bukkit.getPlayer(member.player)
                            if (player != null && player.isOnline) {
                                player.send {
                                    localization(
                                        player = player,
                                        tags = {
                                            parsedPlaceholder("guild", guild.displayName)
                                            parsedPlaceholder("player", clickContext.viewer.name)
                                        }
                                    ) {
                                        this.guild.guildKick
                                    }
                                }
                            } else {
                                DB.trans {
                                    GuildKickNotifyDao.new {
                                        this.guildId = guild.gid
                                        this.guildName = guild.displayName
                                        this.player = member.player
                                        this.operator = clickContext.viewer.uniqueId
                                        this.kickAt = LocalDateTime.now()
                                    }
                                }
                            }
                            clickContext.stack.pop()
                        }
                    }
                }
            }
        }
    }
}