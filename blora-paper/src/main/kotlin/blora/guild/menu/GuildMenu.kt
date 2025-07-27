@file:Suppress("UnstableApiUsage")

package blora.guild.menu

import blora.configuration.CONF
import blora.database.DB
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.dialog.joinGuildDialog
import blora.guild.menu.createguild.createMenuPage
import blora.guild.menu.guildinvite.guildInviteMenu
import blora.guild.menu.guildlist.guildListMenu
import blora.guild.menu.guildview.guildViewMenu
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.builder.limitedDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Material
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildMenu(viewer: Player): Menu {
    return Menu(
        viewer,
        5,
        200L
    ).apply {
        this.closer {
            it.destroy()
        }
        this.stack.push {
            limitedDynamicMenuPage(this) {
                pageId {
                    "guild"
                }
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuildTitle
                    }
                }
                2 to 2 eq {
                    icon { material { Material.ITEM_FRAME } }
                    name {
                        localization(viewer) {
                            this.guild.menu.menuGuildButtonPublic_guilds
                        }
                    }
                    clickEvent { clickContext ->
                        clickContext.stack.push {
                            guildListMenu(
                                clickContext.menu,
                                "guild_list",
                                component {
                                    localization(clickContext.viewer) {
                                        this.guild.menu.menuGuild_listTitle
                                    }
                                },
                                systemFilter = { player, guild ->
                                    ((guild.public || guild.members.contains(viewer.uniqueId)) &&
                                            !guild.blocklist.contains(viewer.uniqueId)) ||
                                            viewer.hasPermission(Permissions.Admin)
                                }
                            )
                        }
                    }
                }
                2 to 5 eq {
                    icon { material { Material.KELP } }
                    name {
                        localization(viewer) {
                            this.guild.menu.menuGuildButtonMy_guilds
                        }
                    }
                    clickEvent { clickContext ->
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
                                guildViewMenu(
                                    clickContext.menu,
                                    guilds[0]
                                )
                            )
                        } else {
                            clickContext.stack.push(
                                guildListMenu(
                                    clickContext.menu,
                                    "my_guild_list",
                                    component {
                                        localization(clickContext.viewer) {
                                            this.guild.menu.menuMy_guildTitle
                                        }
                                    },
                                    systemFilter = { player, guild ->
                                        guild.members.contains(viewer.uniqueId)
                                    }
                                )
                            )
                        }
                    }
                }
                2 to 8 eq {
                    icon { material { Material.GLOW_BERRIES } }
                    name {
                        localization(viewer) {
                            this.guild.menu.menuGuildButtonInvitation
                        }
                    }
                    clickEvent { clickContext ->
                        clickContext.stack.push {
                            guildInviteMenu(clickContext.menu)
                        }
                    }
                }
                4 to 2 eq {
                    icon { material { Material.WHEAT_SEEDS } }
                    name {
                        localization(viewer) {
                            this.guild.menu.menuGuildButtonJoin
                        }
                    }
                    clickEvent { clickContext ->
                        clickContext.viewer.openDialog(
                            joinGuildDialog(viewer)
                        )
                    }
                }
                4 to 5 eq {
                    icon { material { Material.END_CRYSTAL } }
                    name {
                        localization(viewer) {
                            this.guild.menu.menuGuildButtonCreate
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
                        if (playerJoinedGuilds.filter { it.owner == viewer.uniqueId }.size >= CONF.guild.playerMaxOwn) {
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
                        clickContext.stack.push {
                            createMenuPage(clickContext.menu)
                        }
                    }
                }
            }
        }
    }
}