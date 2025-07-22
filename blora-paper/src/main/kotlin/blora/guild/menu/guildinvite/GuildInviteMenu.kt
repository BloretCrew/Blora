package blora.guild.menu.guildinvite

import blora.configuration.CONF
import blora.database.DB
import blora.extension.localization
import blora.guild.GuildJoinSource
import blora.guild.GuildJoinStrategy
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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildInviteMenu(viewer: Player, currentPage: Int = 1): SimpleMenuPage {
    val preInvites = DB.listGuildInvites(viewer.uniqueId)
    val guilds = preInvites.map { it.guildId }
        .mapNotNull { DB.getGuildByGid(it)}
        .associateBy { it.gid }
    val invites = preInvites.filter {
        val guild = guilds[it.guildId]
        if (guild != null) {
            if (guild.blocklist.contains(viewer.uniqueId)) {
                DB.trans {
                    it.delete()
                }
                return@filter false
            }
        }
        return@filter true
    }
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer
            ) {
                this.guild.menu.menuGuild_inviteTitle
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
                    it.stack.push(guildInviteMenu(viewer, currentPage - 1))
                }
            }
        }

        if (invites.size > (currentPage * 21)) {
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
                    it.stack.push(guildInviteMenu(viewer, currentPage + 1))
                }
            }
        }

        invites.forEachIndexed { index, invitation ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            val guild = guilds[invitation.guildId]
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                if (guild == null) {
                    icon(ItemStack(Material.BARRIER))
                    hoverText {
                        title {
                            localization(viewer) {
                                this.guild.menu.inviteMenu.invitationError
                            }
                        }
                    }
                } else {
                    icon(guild.parsedIcon)
                    hoverText {
                        title {
                            localization(viewer) {
                                guild.displayName
                            }
                        }
                        description {
                            newline()
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("inviter", DB.getPlayerDisplayName(invitation.inviter))
                                }
                            ) {
                                this.guild.menu.inviteMenu.invitationDescriptionInviter
                            }
                            newline()
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("invited_at", invitation.invitedAt.castString())
                                }
                            ) {
                                this.guild.menu.inviteMenu.invitationDescriptionInvited_at
                            }
                            newline()
                            newline()
                            localization(
                                player = viewer
                            ) {
                                this.guild.menu.inviteMenu.invitationDescriptionLeft_click
                            }
                            newline()
                            localization(
                                player = viewer
                            ) {
                                this.guild.menu.inviteMenu.invitationDescriptionRight_click
                            }
                        }
                    }
                    clickEvent { menuPageContext ->
                        if (!menuPageContext.clickType.isLeftClick && !menuPageContext.clickType.isRightClick)
                            return@clickEvent
                        if (menuPageContext.clickType.isLeftClick && DB.listGuildForPlayer(viewer.uniqueId).size >= CONF.guild.playerMaxJoin) {
                            viewer.send {
                                localization(
                                    player = viewer,
                                    tags = {
                                        parsedPlaceholder("amount", CONF.guild.playerMaxJoin.toString())
                                    }
                                ) {
                                    this.guild.guildJoinLimit
                                }
                            }
                            return@clickEvent
                        }
                        if (menuPageContext.clickType.isLeftClick) {
                            if (guild.joinStrategy == GuildJoinStrategy.DIRECT || guild.joinStrategy == GuildJoinStrategy.INVITE_DIRECT) {
                                DB.guildJoinDirect(guild, viewer.uniqueId, GuildJoinSource.InviteByPlayer(invitation.inviter))
                                DB.announceJoin(guild, viewer)
                                viewer.send {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("guild", guild.displayName)
                                        }
                                    ) {
                                        this.guild.guildJoinSuccess
                                    }
                                }
                            } else if (guild.joinStrategy == GuildJoinStrategy.REQUIRE_REVIEW) {
                                DB.createJoinRequest(guild, viewer, GuildJoinSource.InviteByPlayer(invitation.inviter))
                                DB.announceJoinRequest(guild, viewer)
                                viewer.send {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("guild", guild.displayName)
                                            parsedPlaceholder("inviter", DB.getPlayerDisplayName(invitation.inviter))
                                        }
                                    ) {
                                        this.guild.guildInvite_joinAccept
                                    }
                                }
                            } else {
                                viewer.send {
                                    localization(viewer) {
                                        this.guild.menu.inviteMenu.invitationGuild_not_allow_join
                                    }
                                }
                            }
                        } else {
                            viewer.send {
                                localization(
                                    player = viewer,
                                    tags = {
                                        parsedPlaceholder("guild", guild.displayName)
                                        parsedPlaceholder("inviter", DB.getPlayerDisplayName(invitation.inviter))
                                    }
                                ) {
                                    this.guild.guildInvite_joinReject
                                }
                            }
                        }
                        DB.trans {
                            invitation.delete()
                        }
                        if ((invites.size - 1) <= (currentPage - 1) * 21 - 1) { // this page does no longer exist
                            menuPageContext.stack.replace(guildInviteMenu(viewer, currentPage - 1))
                        } else {
                            menuPageContext.stack.replace(guildInviteMenu(viewer, currentPage))
                        }
                    }
                }
            }
        }
    }
}