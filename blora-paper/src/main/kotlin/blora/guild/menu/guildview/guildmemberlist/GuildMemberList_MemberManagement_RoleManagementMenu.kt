package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildMemberInfoDao
import blora.extension.localization
import blora.guild.dataprovider.GuildRoleDaoDataProvider
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
import org.bukkit.Material
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildMemberList_memberManagement_roleManagementMenu(
    menu: Menu,
    guild: GuildDao,
    member: GuildMemberInfoDao
): MenuPage<*, *> {
    val guildId = guild.id
    val memberId = member.id
    return pageableMenuPage(menu, GuildRoleDaoDataProvider(guild.gid)) {
        val cachedPlayerName = DB.getPlayerDisplayName(member.player)
        pageId {
            "guild_${guildId}_memberList_memberManagement_${memberId}_roleManagement"
        }
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
        showBackButton()
        dataItem { viewContext, role ->
            icon { material { Material.PAPER } }
            name {
                localization(viewContext.viewer) {
                    role.displayName
                }
            }
            description {
                newline()
                localization(
                    player = viewContext.viewer,
                    tags = {
                        parsedPlaceholder("status", role.ownedMembers.contains(member.player).toString())
                    }
                ) {
                    this.guild.menu.menuGuild_member_listMember_managementRole_managementDescription1
                }
                if (role.roleId != "member") {
                    newline()
                    localization(viewContext.viewer) {
                        this.guild.menu.menuGuild_member_listMember_managementRole_managementDescription2
                    }
                }
            }
            clickEvent { clickContext ->
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
                clickContext.menu.rerender()
            }
        }
    }
}