@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildmemberlist

import blora.database.DB
import blora.database.guild.dao.GuildDao
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
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildMemberList_blocklistMenu(viewer: Player, guild: GuildDao, currentPage: Int = 1): SimpleMenuPage {
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_member_listJoin_requestsTitle
            }
        }

        mapping(
            "#########",
            "#       #",
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

        5 to 5 eq {

        }

        if (guild.blocklist.size <= (currentPage - 1) * 21 - 1)
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
                    it.stack.push(guildMemberList_blocklistMenu(viewer, guild,  currentPage - 1))
                }
            }
        }

        if (guild.blocklist.size > (currentPage * 21)) {
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
                    it.stack.push(guildMemberList_blocklistMenu(viewer, guild,  currentPage + 1))
                }
            }
        }

        guild.blocklist.forEachIndexed { index, blocked ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(
                    ItemStack(Material.PLAYER_HEAD).apply {
                        this.setData(
                            DataComponentTypes.PROFILE,
                            ResolvableProfile.resolvableProfile(Bukkit.getOfflinePlayer(blocked).playerProfile)
                        )
                    }
                )
                hoverText {
                    title {
                        DB.getPlayerDisplayName(blocked)
                    }
                    description {
                        newline()
                        localization(viewer) {
                            this.guild.menu.menuGuild_member_listBlocklistItemDescription
                        }
                    }
                }
                clickEvent {
                    if (it.clickType.isRightClick) {
                        DB.trans {
                            guild.blocklist = guild.blocklist.toMutableList().apply { remove(blocked) }.toList()
                            guild.flush()
                        }
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("guild", guild.displayName)
                                    parsedPlaceholder("player", DB.getPlayerDisplayName(blocked))
                                }
                            ) {
                                this.guild.guildBlocklistRemove
                            }
                        }
                        if (guild.blocklist.size <= (currentPage - 1) * 21 - 1) { // this page does no longer exist
                            it.stack.replace(guildMemberList_blocklistMenu(viewer, guild, currentPage - 1))
                        } else {
                            it.stack.replace(guildMemberList_blocklistMenu(viewer, guild, currentPage))
                        }
                    }
                }
            }
        }
    }
}