@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.extension.resolvableProfile
import blora.guild.GuildPermissions
import blora.guild.dataprovider.GuildMemberDaoWithRolePriorityDataProvider
import blora.guild.menu.guildview.guildinvitationcode.guildInvitationCodeMenu
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import blora.util.castString
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text

fun guildMemberListMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, GuildMemberDaoWithRolePriorityDataProvider(guild)) {
        DB.trans {
            guild.refresh()
        }
        val isMember = guild.members.contains(menu.viewer.uniqueId)
        val permissions = guild.getPlayerPermissions(menu.viewer.uniqueId, isMember)
        pageId {
            "guild_${guildId}_memberList"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_member_listTitle
            }
        }
        showBackButton()

        if (permissions.contains(GuildPermissions.MANAGE_INVITATION_CODE)) {
            5 to 3 eq {
                icon { material { Material.NAME_TAG } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_viewButtonInvitation_codes
                    }
                }
                clickEvent {
                    it.stack.push {
                        guildInvitationCodeMenu(menu, guild)
                    }
                }
            }
        }

        if (permissions.contains(GuildPermissions.INVITE_PLAYER)) {
            5 to 4 eq {
                icon { material { Material.PAPER } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_viewButtonInvite_player
                    }
                }
                clickEvent { clickContext ->
                    if (guild.members.size >= guild.maxMembers) {
                        clickContext.viewer.send {
                            localization(clickContext.viewer) {
                                this.guild.guildJoin_invite_reviewMembers_limit
                            }
                        }
                        return@clickEvent
                    }
                    clickContext.stack.push {
                        guildMemberList_inviteMenu(menu, guild)
                    }
                }
            }
        }

        if (permissions.contains(GuildPermissions.REVIEW_PLAYER)) {
            val requests = DB.listValidJoinRequestsForGuild(guild.gid).filter { !it.finished }.toMutableList()
            5 to 5 eq {
                icon { material { Material.BOOK } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_member_listButtonJoin_request
                    }
                }
                description {
                    localization(
                        menu.viewer,
                        tags = {
                            parsedPlaceholder("requests", requests.size.toString())
                        }
                    ) {
                        this.guild.menu.menuGuild_member_listButtonJoin_requestDescription
                    }
                }
                clickEvent { clickContext ->
                    clickContext.stack.push {
                        guildMemberList_joinRequestsMenu(menu, guild)
                    }
                }
            }
        }

        if (permissions.contains(GuildPermissions.BLOCKLIST)) {
            5 to 6 eq {
                icon { material { Material.BLACK_DYE } }
                name {
                    localization(menu.viewer) {
                        this.guild.menu.menuGuild_member_listButtonBlocklist
                    }
                }
                clickEvent { clickContext ->
                    clickContext.stack.push {
                        guildMemberList_blocklistMenu(menu, guild)
                    }
                }
            }
        }

        val viewerPriority = DB.getMaxRolePriority(menu.viewer.uniqueId, guild)

        dataItem { viewContext, (memberInfo, playerPriority), dataIndex ->
            val cachedPlayerName = DB.getPlayerDisplayName(memberInfo.player)
            val roles = DB.listRolesForPlayer(memberInfo.player, guild.gid)
            icon {
                material { Material.PLAYER_HEAD }
                DataComponentTypes.PROFILE eq memberInfo.player.resolvableProfile()
            }
            name {
                text { cachedPlayerName }
            }
            description {
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        if (memberInfo.player == guild.owner) {
                            componentPlaceholder("role") {
                                localization(menu.viewer) {
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
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("join_at", memberInfo.joinAt.castString())
                    }
                ) {
                    this.guild.menu.menuGuild_member_listPlayerDescriptionJoin_at
                }
            }
            clickEvent { clickContext ->
                if (clickContext.viewer.uniqueId != memberInfo.player &&
                    (clickContext.viewer.uniqueId == guild.owner || viewerPriority > playerPriority)
                ) {
                    clickContext.stack.push {
                        guildMemberList_memberManagementMenu(menu, guild, memberInfo)
                    }
                }
            }
        }
    }
}