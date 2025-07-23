package blora.guild.menu.createguild

import blora.configuration.CONF
import blora.database.DB
import blora.extension.localization
import blora.guild.CreatingGuildContext
import blora.guild.GuildModule
import blora.guild.dialog.createguild.createGuild_modifyDisplayName
import blora.guild.dialog.createguild.createGuild_setIdDialog
import blora.guild.menu.guildview.guildViewMenu
import blora.item.clone
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.inventoryClick
import blora.menu.v2.page.builder.limitedDynamicMenuPage
import blora.menu.v2.page.builder.title
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun createMenuPage(menu: Menu): MenuPage<*, *> {
    val context = CreatingGuildContext()
    return limitedDynamicMenuPage(menu) {
        title {
            localization(menu.viewer) {
                this.guild.menu.menuCreate_guildTitle
            }
        }
        inventoryClick { item, clickContext ->
            context.icon = item
            clickContext.menu.rerender()
            return@inventoryClick true
        }
        backButton()
        3 to 2 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuCreate_guildButtonSet_id
                }
            }
            if (context.id.isNotEmpty()) {
                description {
                    newline()
                    mini("<italic:false><white>${context.id}")
                }
            }
            clickEvent { clickContext ->
                createGuild_setIdDialog(clickContext.viewer, context, { clickContext.menu.rerender() })
            }
        }
        3 to 5 eq {
            icon { material { Material.PAPER } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuCreate_guildButtonModify_display_name
                }
            }
            if (context.displayName.isNotEmpty()) {
                description {
                    newline()
                    localization(menu.viewer) {
                        context.displayName
                    }
                }
            }
            clickEvent { clickContext ->
                createGuild_modifyDisplayName(clickContext.viewer, context, { clickContext.menu.rerender() })
            }
        }
        3 to 8 eq {
            icon { clone { context.icon } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuCreate_guildButtonModify_icon
                }
            }
            description {
                newline()
                localization(menu.viewer) {
                    "<italic:false><white>${this.guild.menu.menuCreate_guildButtonModify_iconTooltip}"
                }
            }
        }

        5 to 9 eq {
            icon { material { Material.EMERALD } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuCreate_guildButtonCreate
                }
            }
            clickEvent { clickContext ->
                val viewer = clickContext.viewer
                if (!viewer.hasPermission(Permissions.Guild.Create)) {
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildWarningNo_permission_to_create
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
                if (context.id.isEmpty()) {
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildWarningId_cannot_be_empty
                        }
                    }
                    return@clickEvent
                }
                if (BloraPlugin.database.getGuildByGid(context.id) != null) {
                    // because a guild can be created when player editing
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildWarningId_exists
                        }
                    }
                    return@clickEvent
                }
                val guild = GuildModule.createGuild(viewer, context)
                PlayerPoints.getInstance().api.take(viewer.uniqueId, CONF.guild.createCost)
                viewer.send {
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder("guild_name", context.displayName.ifEmpty { context.id })
                            parsedPlaceholder("guild_id", context.id)
                        }
                    ) {
                        this.guild.guildCreateSuccess
                    }
                }
                clickContext.stack.replace {
                    guildViewMenu(menu, guild)
                }
            }
        }
    }
}