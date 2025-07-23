@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.extension.resolvableProfile
import blora.guild.dataprovider.GuildBlocklistDaoDataProvider
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.dataItem
import blora.menu.v2.page.builder.pageableMenuPage
import blora.menu.v2.page.builder.showBackButton
import blora.menu.v2.page.builder.title
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text

fun guildMemberList_blocklistMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    return pageableMenuPage(menu, GuildBlocklistDaoDataProvider(guild.gid)) {
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_member_listJoin_requestsTitle
            }
        }
        showBackButton()
        5 to 5 eq {
            icon { material { Material.IRON_SWORD } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_member_listBlocklistButtonAdd
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.push {
                    guildMemberList_blocklist_addBlockedMenu(menu, guild)
                }
            }
        }

        dataItem { viewContext, blocked ->
            icon {
                material { Material.PLAYER_HEAD }
                DataComponentTypes.PROFILE eq blocked.player.resolvableProfile()
            }
            name {
                text { DB.getPlayerDisplayName(blocked.player) }
            }
            description {
                newline()
                localization(viewContext.viewer) {
                    this.guild.menu.menuGuild_member_listBlocklistItemDescription
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isRightClick) {
                    DB.trans {
                        guild.blocklist = guild.blocklist.toMutableList().apply { remove(blocked.player) }.toList()
                        guild.flush()

                        blocked.delete()
                    }
                    clickContext.viewer.send {
                        localization(
                            player = clickContext.viewer,
                            tags = {
                                parsedPlaceholder("guild", guild.displayName)
                                parsedPlaceholder("player", DB.getPlayerDisplayName(blocked.player))
                            }
                        ) {
                            this.guild.guildBlocklistRemove
                        }
                    }
                    clickContext.menu.rerender()
                }
            }
        }
    }
}