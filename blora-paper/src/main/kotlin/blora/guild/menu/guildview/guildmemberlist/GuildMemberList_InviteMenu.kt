@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildInvitationDao
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
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text
import java.time.LocalDateTime

fun guildMemberList_inviteMenu(viewer: Player, guild: GuildDao, requestCurrentPage: Int = 1): SimpleMenuPage {
    val invitable = Bukkit.getOnlinePlayers()
        .filter { it != viewer }
        .filter { !guild.members.contains(it.uniqueId) }
        .filter { it.isOnline }
        .filter { DB.getJoinRequest(guild.gid, viewer.uniqueId) == null }
        .filter { !DB.isInvited(viewer.uniqueId, guild.gid) }
        .toMutableList()

    var currentPage = requestCurrentPage

    while (invitable.size <= (currentPage - 1) * 21 - 1 && currentPage > 1)
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
                this.guild.menu.menuGuild_viewInviteTitle
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
                    it.stack.pop()
                    it.stack.push(guildMemberList_inviteMenu(viewer, guild, currentPage - 1))
                }
            }
        }

        if (invitable.size > (currentPage * 21)) {
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
                    it.stack.push(guildMemberList_inviteMenu(viewer, guild, currentPage + 1))
                }
            }
        }

        invitable.forEachIndexed { index, invitablePlayer ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(ItemStack(Material.PLAYER_HEAD).apply {
                    this.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(invitablePlayer.playerProfile))
                })
                hoverText {
                    title {
                        text { invitablePlayer.name }
                    }
                }
                clickEvent {
                    if (guild.blocklist.contains(invitablePlayer.uniqueId)) {
                        viewer.send {
                            localization(viewer) {
                                this.guild.guildOtherBlocked
                            }
                        }
                        return@clickEvent
                    }
                    DB.trans {
                        GuildInvitationDao.new {
                            this.guildId = guild.gid
                            this.invitee = invitablePlayer.uniqueId
                            this.inviter = viewer.uniqueId
                            this.invitedAt = LocalDateTime.now()
                        }
                    }
                    viewer.send {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("guild", guild.displayName)
                                parsedPlaceholder("invitee", invitablePlayer.name)
                            }
                        ) {
                            this.guild.guildInviteInviter
                        }
                    }
                    invitablePlayer.send {
                        localization(
                            player = invitablePlayer,
                            tags = {
                                parsedPlaceholder("guild", guild.displayName)
                                parsedPlaceholder("inviter", viewer.name)
                            }
                        ) {
                            this.guild.guildInviteInvitee
                        }
                    }
                    if (invitable.size <= (currentPage - 1) * 21 - 1) { // this page does no longer exist
                        it.stack.replace(guildMemberList_inviteMenu(viewer, guild, currentPage - 1))
                    } else {
                        it.stack.replace(guildMemberList_inviteMenu(viewer, guild, currentPage))
                    }
                }
            }
        }
    }
}