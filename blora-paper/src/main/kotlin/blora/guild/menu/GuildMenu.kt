@file:Suppress("UnstableApiUsage")

package blora.guild.menu

import blora.configuration.CONF
import blora.database.DB
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.dialog.joinGuildDialog
import blora.guild.menu.createguild.createMenuPage
import blora.guild.menu.guildinvite.guildInviteMenu
import blora.guild.menu.guildlist.guildListMenuPage
import blora.guild.menu.guildview.guildView
import blora.menu.Menu
import blora.menu.clickEvent
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.title
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildMenu(viewer: Player): Menu {
    return Menu(
        null,
        viewer,
        5,
        component {
            localization(viewer) {
                this.guild.menu.menuGuildTitle
            }
        },
        {
            it.destroy()
        }
    ) {
        lines(5)
        title {
            localization(viewer) {
                this.guild.menu.menuGuildTitle
            }
        }

        2 to 2 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuildButtonPublic_guilds
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    clickContext.stack.push(
                        guildListMenuPage(
                            viewer,
                            true,
                            systemFilter = { player, guild ->
                                ((guild.public || guild.members.contains(viewer.uniqueId)) &&
                                        !guild.blocklist.contains(viewer.uniqueId)) ||
                                        viewer.hasPermission(Permissions.Admin)
                            }
                        )
                    )
                }
            }
        }

        2 to 4 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuildButtonMy_guilds
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    val guilds = BloraPlugin.database.listGuildForPlayer(viewer.uniqueId)
                    if (guilds.isEmpty()) {
                        viewer.send {
                            localization(viewer) {
                                this.guild.guildWarningNot_join_any_guilds
                            }
                        }
                    } else if (guilds.size == 1) {
                        // open guild page directly
                        clickContext.stack.push(
                            guildView(
                                viewer,
                                guilds[0],
                                mutableListOf()
                            ) {}
                        )
                    } else {
                        clickContext.stack.push(
                            guildListMenuPage(
                                viewer,
                                true,
                                systemFilter = { player, guild ->
                                    guild.members.contains(viewer.uniqueId)
                                }
                            )
                        )
                    }
                }
            }
        }

        2 to 6 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuildButtonInvitation
                    }
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.push(guildInviteMenu(viewer))
            }
        }

        2 to 8 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuildButtonJoin
                    }
                }
            }
            clickEvent {
                viewer.openDialog(
                    joinGuildDialog(viewer)
                )
            }
        }

        4 to 2 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuildButtonCreate
                    }
                }
            }
            clickEvent { clickContext ->
                if (!viewer.hasPermission(Permissions.Guild.Create)) {
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildWarningNo_permission_to_create
                        }
                    }
                    return@clickEvent
                }
                if (clickContext.clickType.isLeftClick) {
                    if (PlayerPoints.getInstance().api.look(viewer.uniqueId) < CONF.guild.createCost) {
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("cost", CONF.guild.createCost.toString())
                                }
                            ) {
                                this.guild.guildCreateMoney_not_enough
                            }
                        }
                        return@clickEvent
                    }
                    val playerJoinedGuilds = DB.listGuildForPlayer(viewer.uniqueId)
                    if (playerJoinedGuilds.size >= CONF.guild.playerMaxJoin) {
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
                    if (playerJoinedGuilds.filter { it.owner  == viewer.uniqueId }.size >= CONF.guild.playerMaxOwn) {
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("amount", CONF.guild.playerMaxOwn.toString())
                                }
                            ) {
                                this.guild.guildCreateLimit
                            }
                        }
                        return@clickEvent
                    }
                    clickContext.stack.push(
                        createMenuPage(
                            viewer
                        )
                    )
                }
            }
        }
    }
}