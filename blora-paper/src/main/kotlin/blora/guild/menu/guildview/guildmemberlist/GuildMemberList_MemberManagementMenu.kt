@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildMemberInfoDao
import blora.database.guild.table.GuildInvitationTable
import blora.database.guild.table.GuildJoinNotifyTable
import blora.database.guild.table.GuildJoinRequestTable
import blora.database.guild.table.GuildMemberInfoTable
import blora.extension.localization
import blora.guild.GuildPermissions
import blora.guild.menu.guildMenu
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.confirmationMenuLine5
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
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
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.time.Duration.Companion.days

private val buttons = listOf(
    4 to 2,
    4 to 4,
    4 to 6,
    4 to 8,
)

fun guildMemberList_memberManagementMenu(viewer: Player, guild: GuildDao, member: GuildMemberInfoDao, permissions: Collection<GuildPermissions>, rerenderCallback: () -> Unit): SimpleMenuPage {
    val cachedPlayerName = DB.getPlayerDisplayName(member.player)
    val viewerPriority = DB.getMaxRolePriority(viewer.uniqueId, guild)
    val playerPriority = DB.getMaxRolePriority(member.player, guild)
    val isOwner = viewer.uniqueId == guild.owner
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                    parsedPlaceholder("player", cachedPlayerName)
                }
            ) {
                this.guild.menu.menuGuild_member_listMember_managementTitle
            }
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

        2 to 5 eq {
            icon(ItemStack(Material.PLAYER_HEAD).apply {
                this.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(Bukkit.getOfflinePlayer(member.player).playerProfile))
            })
            hoverText {
                title {
                    text { cachedPlayerName }
                }
            }
        }

        var buttonIndex = 0

        if (isOwner) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.NETHER_STAR))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_member_listMember_managementButtonTransfer_owner
                        }
                    }
                }
                clickEvent {
                    if (CONF.guild.ownerTransferCooldownDays > 0) {
                        if (guild.lastOwnerTransferDate.plusDays(CONF.guild.ownerTransferCooldownDays.toLong()) >= LocalDate.now()) {
                            viewer.send {
                                localization(
                                    player = viewer,
                                    tags = {
                                        parsedPlaceholder(
                                            "days",
                                            abs(ChronoUnit.DAYS.between(
                                                guild.lastOwnerTransferDate.plusDays(CONF.guild.ownerTransferCooldownDays.toLong()),
                                                LocalDate.now()
                                            )).toString()
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
                    it.menu.destroy()
                    guildMenu(viewer).open() // reopen after transfer because it has many dangerous permission issues
                }
            }
            buttonIndex++
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.BOOKSHELF))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_member_listMember_managementButtonModify_roles
                        }
                    }
                }
                clickEvent {
                    it.stack.push(guildMemberList_memberManagement_roleManagementMenu(viewer, guild, member))
                }
            }
            buttonIndex++
        }
        if (member.player != viewer.uniqueId && viewerPriority > playerPriority) {
            buttons[buttonIndex] eq {
                icon(ItemStack(Material.BARRIER))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_member_listMember_managementButtonKick
                        }
                    }
                }
                clickEvent {
                    it.stack.push(confirmationMenuLine5(
                        viewer,
                        component {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("player", cachedPlayerName)
                                }
                            ) {
                                this.guild.menu.menuGuild_member_listMember_managementKickTitle
                            }
                        }
                    ) {
                        val roles = DB.listRolesForPlayer(member.player, guild.gid)
                        DB.trans {
                            for (role in roles) {
                                role.ownedMembers = role.ownedMembers.toMutableList().apply { remove(member.player) }.toList()
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
                                    player = viewer,
                                    tags = {
                                        parsedPlaceholder("guild", guild.displayName)
                                        parsedPlaceholder("player", viewer.name)
                                    }
                                ) {
                                    this.guild.guildKick
                                }
                            }
                        } else {
                            // todo: kick notify for offline player
                        }
                        it.stack.pop()
                        rerenderCallback()
                    })
                }
            }
        }
    }
}