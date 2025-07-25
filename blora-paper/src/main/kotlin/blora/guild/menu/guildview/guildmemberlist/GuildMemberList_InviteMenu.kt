@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildInvitationDao
import blora.extension.localization
import blora.extension.resolvableProfile
import blora.guild.dataprovider.OnlinePlayerDataProvider
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.dataItem
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.pageableMenuPage
import blora.menu.v2.page.builder.title
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text
import java.time.LocalDateTime

fun guildMemberList_inviteMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, OnlinePlayerDataProvider { player ->
        (player != menu.viewer) &&
                (!guild.members.contains(player.uniqueId)) &&
                (!guild.blocklist.contains(player.uniqueId)) &&
                (DB.getValidJoinRequest(guild.gid, player.uniqueId) == null) &&
                (!DB.isInvited(player.uniqueId, guild.gid)) &&
                (player.isOnline)
    }) {
        pageId {
            "guild_${guildId}_memberList_invite"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_viewInviteTitle
            }
        }
        backButton()

        dataItem { viewContext, invitablePlayer ->
            icon {
                material { Material.PLAYER_HEAD }
                DataComponentTypes.PROFILE eq invitablePlayer.resolvableProfile()
            }
            name {
                text { invitablePlayer.name }
            }
            clickEvent { clickContext ->
                if (!DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid).invitePlayer) {
                    clickContext.stack.pop()
                    return@clickEvent
                }
                if (guild.blocklist.contains(invitablePlayer.uniqueId)) {
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.guild.guildOtherBlocked
                        }
                    }
                    return@clickEvent
                }
                if (DB.getValidJoinRequest(guild.gid, invitablePlayer.uniqueId) != null) {
                    clickContext.menu.rerender()
                    return@clickEvent
                }
                if (DB.isInvited(invitablePlayer.uniqueId, guild.gid)) {
                    clickContext.menu.rerender()
                    return@clickEvent
                }
                DB.trans {
                    GuildInvitationDao.new {
                        this.guildId = guild.gid
                        this.invitee = invitablePlayer.uniqueId
                        this.inviter = clickContext.viewer.uniqueId
                        this.invitedAt = LocalDateTime.now()
                    }.flush()
                }
                clickContext.viewer.send {
                    localization(
                        player = clickContext.viewer,
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
                            parsedPlaceholder("inviter", clickContext.viewer.name)
                        }
                    ) {
                        this.guild.guildInviteInvitee
                    }
                }
                clickContext.menu.rerender()
            }
        }
    }
}