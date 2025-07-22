@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.guild.GuildPermissions
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.mapping
import blora.menu.menuPage
import blora.menu.title
import blora.util.castString
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text

fun guildMemberListMenu(viewer: Player, guild: GuildDao, permissions: Collection<GuildPermissions>, currentPage: Int = 1): SimpleMenuPage {
    val members = guild.members.toMutableList()
    val memberMaxRolePriority = DB.listRolePriorities(guild)
    val viewerPriority = memberMaxRolePriority[viewer.uniqueId]
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_member_listTitle
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

        if (members.size <= (currentPage - 1) * 21 - 1)
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
                    it.stack.replace(guildMemberListMenu(viewer, guild, permissions,  currentPage - 1))
                }
            }
        }

        if (members.size > (currentPage * 21)) {
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
                    it.stack.replace(guildMemberListMenu(viewer, guild, permissions,  currentPage + 1))
                }
            }
        }

        if (permissions.contains(GuildPermissions.REVIEW_PLAYER)) {
            val requests = DB.listJoinRequestsForGuild(guild.gid).filter { !it.finished }.toMutableList()
            5 to 5 eq {
                icon(ItemStack(Material.BOOK))
                hoverText {
                    title {
                        localization(
                            viewer,
                            tags = {
                                parsedPlaceholder("requests", requests.size.toString())
                            }
                        ) {
                            this.guild.menu.menuGuild_member_listButtonJoin_requestDescription
                        }
                    }
                }
                clickEvent {
                    it.stack.push(guildMemberList_joinRequestsMenu(viewer, guild, requests))
                }
            }
        }

        if (permissions.contains(GuildPermissions.INVITE_PLAYER)) {
            5 to 4 eq {
                icon(ItemStack(Material.PAPER))
                hoverText {
                    title {
                        localization(viewer) {
                            this.guild.menu.menuGuild_viewButtonInvite_player
                        }
                    }
                }
                clickEvent { menuPageContext ->
                    menuPageContext.stack.push(guildMemberList_inviteMenu(viewer, guild))
                }
            }
        }

        members.forEachIndexed { index, memberUuid ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            val cachedPlayerName = DB.getPlayerDisplayName(memberUuid)
            val memberInfo = DB.getMemberInfo(guild.gid, memberUuid)
            val roles = DB.listRolesForPlayer(memberUuid, guild.gid)
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(ItemStack(Material.PLAYER_HEAD).apply {
                    this.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(Bukkit.getOfflinePlayer(memberUuid).playerProfile))
                })
                hoverText {
                    title {
                        text { cachedPlayerName }
                    }
                    description {
                        if (memberInfo == null) {
                            newline()
                            localization(viewer) {
                                this.guild.menu.menuGuild_member_listPlayerDescriptionError
                            }
                        } else {
                            newline()
                            localization(
                                player = viewer,
                                tags = {
                                    if (memberUuid == guild.owner) {
                                        componentPlaceholder("role") {
                                            localization(viewer) {
                                                this.guild.menu.menuGuild_member_listPlayerDescriptionOwner
                                            }
                                        }
                                    } else {
                                        parsedPlaceholder("role", roles.first().displayName)
                                    }
                                }
                            ) {
                                this.guild.menu.menuGuild_member_listPlayerDescriptionRole
                            }
                            newline()
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("join_at", memberInfo.joinAt.castString())
                                }
                            ) {
                                this.guild.menu.menuGuild_member_listPlayerDescriptionJoin_at
                            }
                        }
                    }
                }
                clickEvent {
                    val priority = memberMaxRolePriority[memberUuid]
                    if (viewer.uniqueId != memberUuid &&
                        (viewer.uniqueId == guild.owner || (viewerPriority != null && priority != null && viewerPriority > priority)) &&
                        memberInfo != null) {
                        it.stack.push(guildMemberList_memberManagementMenu(viewer, guild, memberInfo, permissions) {
                            if (guild.members.size <= (currentPage - 1) * 21 - 1) { // this page does no longer exist
                                it.stack.replace(guildMemberListMenu(viewer, guild, permissions, currentPage - 1))
                            } else {
                                it.stack.replace(guildMemberListMenu(viewer, guild, permissions, currentPage))
                            }
                        })
                    }
                }
            }
        }
    }
}