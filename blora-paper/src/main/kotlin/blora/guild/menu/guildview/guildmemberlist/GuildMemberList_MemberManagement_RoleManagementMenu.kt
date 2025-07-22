package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildMemberInfoDao
import blora.extension.localization
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.mapping
import blora.menu.menuPage
import blora.menu.title
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildMemberList_memberManagement_roleManagementMenu(
    viewer: Player,
    guild: GuildDao,
    member: GuildMemberInfoDao,
    currentPage: Int = 1
): SimpleMenuPage {
    val roles = DB.listRolesForGuild(guild.gid)
    val cachedPlayerName = DB.getPlayerDisplayName(member.player)
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
        if (roles.size <= (currentPage - 1) * 21 - 1)
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
                    it.stack.push(guildMemberList_memberManagement_roleManagementMenu(viewer, guild, member,  currentPage - 1))
                }
            }
        }

        if (roles.size > (currentPage * 21)) {
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
                    it.stack.push(guildMemberList_memberManagement_roleManagementMenu(viewer, guild, member,  currentPage + 1))
                }
            }
        }

        roles.forEachIndexed { index, role ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(ItemStack(Material.PAPER))
                hoverText {
                    title {
                        localization(viewer) {
                            role.displayName
                        }
                    }
                    description {
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("status", role.ownedMembers.contains(member.player).toString())
                            }
                        ) {
                            this.guild.menu.menuGuild_member_listMember_managementRole_managementDescription1
                        }
                        if (role.roleId != "member") {
                            newline()
                            localization(viewer) {
                                this.guild.menu.menuGuild_member_listMember_managementRole_managementDescription2
                            }
                        }
                    }
                }
                clickEvent {
                    if (role.roleId == "member")
                        return@clickEvent
                    DB.trans {
                        if (role.ownedMembers.contains(member.player)) {
                            role.ownedMembers = role.ownedMembers.toMutableList().apply { remove(member.player) }.toList()
                        } else {
                            role.ownedMembers = role.ownedMembers.toMutableList().apply { add(member.player) }.toList()
                        }
                        role.flush()
                    }
                    it.menu.rerender()
                }
            }
        }
    }
}