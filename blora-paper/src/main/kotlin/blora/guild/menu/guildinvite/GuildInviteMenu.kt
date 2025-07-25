package blora.guild.menu.guildinvite

import blora.configuration.CONF
import blora.database.DB
import blora.extension.localization
import blora.guild.GuildJoinSource
import blora.guild.GuildJoinStrategy
import blora.guild.dataprovider.GuildInvitationDaoProvider
import blora.item.clone
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.dataItem
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.pageableMenuPage
import blora.menu.v2.page.builder.showBackButton
import blora.menu.v2.page.builder.title
import blora.util.castString
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildInviteMenu(menu: Menu): MenuPage<*, *> {
    return pageableMenuPage(menu, GuildInvitationDaoProvider(menu.viewer.uniqueId)) {
        pageId {
            "guild_invites"
        }
        title {
            localization(
                player = menu.viewer
            ) {
                this.guild.menu.menuGuild_inviteTitle
            }
        }
        showBackButton()
        dataItem { viewContext, invitation ->
            val guild = DB.getGuildByGid(invitation.guildId)
            if (guild == null) {
                icon { material { Material.BARRIER } }
                name {
                    localization(viewContext.viewer) {
                        this.guild.menu.inviteMenu.invitationError
                    }
                }
            } else {
                icon { clone { guild.parsedIcon } }
                name {
                    localization(viewContext.viewer) {
                        guild.displayName
                    }
                }
                description {
                    newline()
                    localization(
                        player = viewContext.viewer,
                        tags = {
                            parsedPlaceholder("inviter", DB.getPlayerDisplayName(invitation.inviter))
                        }
                    ) {
                        this.guild.menu.inviteMenu.invitationDescriptionInviter
                    }
                    newline()
                    localization(
                        player = viewContext.viewer,
                        tags = {
                            parsedPlaceholder("invited_at", invitation.invitedAt.castString())
                        }
                    ) {
                        this.guild.menu.inviteMenu.invitationDescriptionInvited_at
                    }
                    newline()
                    newline()
                    localization(
                        player = viewContext.viewer
                    ) {
                        this.guild.menu.inviteMenu.invitationDescriptionLeft_click
                    }
                    newline()
                    localization(
                        player = viewContext.viewer
                    ) {
                        this.guild.menu.inviteMenu.invitationDescriptionRight_click
                    }
                }
                clickEvent { clickContext ->
                    if (!clickContext.click.isLeftClick && !clickContext.click.isRightClick)
                        return@clickEvent
                    if (clickContext.click.isLeftClick && DB.listGuildForPlayer(clickContext.viewer.uniqueId).size >= CONF.guild.playerMaxJoin) {
                        clickContext.viewer.send {
                            localization(
                                player = clickContext.viewer,
                                tags = {
                                    parsedPlaceholder("amount", CONF.guild.playerMaxJoin.toString())
                                }
                            ) {
                                this.guild.guildJoinLimit
                            }
                        }
                        return@clickEvent
                    }
                    if (clickContext.click.isLeftClick) {
                        if (guild.joinStrategy == GuildJoinStrategy.DIRECT || guild.joinStrategy == GuildJoinStrategy.INVITE_DIRECT) {
                            DB.guildJoinDirect(
                                guild,
                                clickContext.viewer.uniqueId,
                                GuildJoinSource.InviteByPlayer(invitation.inviter)
                            )
                            DB.announceJoin(guild, clickContext.viewer)
                            clickContext.viewer.send {
                                localization(
                                    player = clickContext.viewer,
                                    tags = {
                                        parsedPlaceholder("guild", guild.displayName)
                                    }
                                ) {
                                    this.guild.guildJoinSuccess
                                }
                            }
                        } else if (guild.joinStrategy == GuildJoinStrategy.REQUIRE_REVIEW) {
                            DB.createJoinRequest(
                                guild,
                                clickContext.viewer,
                                GuildJoinSource.InviteByPlayer(invitation.inviter)
                            )
                            DB.announceJoinRequest(guild, clickContext.viewer)
                            clickContext.viewer.send {
                                localization(
                                    player = clickContext.viewer,
                                    tags = {
                                        parsedPlaceholder("guild", guild.displayName)
                                        parsedPlaceholder("inviter", DB.getPlayerDisplayName(invitation.inviter))
                                    }
                                ) {
                                    this.guild.guildInvite_joinAccept
                                }
                            }
                        } else {
                            clickContext.viewer.send {
                                localization(clickContext.viewer) {
                                    this.guild.menu.inviteMenu.invitationGuild_not_allow_join
                                }
                            }
                        }
                    } else {
                        clickContext.viewer.send {
                            localization(
                                player = clickContext.viewer,
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
                    clickContext.menu.rerender()
                }
            }
        }
    }
}