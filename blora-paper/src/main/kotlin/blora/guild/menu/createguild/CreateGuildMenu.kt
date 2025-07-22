package blora.guild.menu.createguild

import blora.configuration.CONF
import blora.database.DB
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.CreatingGuildContext
import blora.guild.GuildModule
import blora.guild.dialog.createguild.createGuild_modifyDisplayName
import blora.guild.dialog.createguild.createGuild_setIdDialog
import blora.guild.menu.guildview.guildView
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.inventoryClick
import blora.menu.lines
import blora.menu.menuPage
import blora.menu.title
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun createMenuPage(viewer: Player, context: CreatingGuildContext = CreatingGuildContext()): SimpleMenuPage {
    return menuPage {
        lines(5)
        title {
            localization(viewer) {
                this.guild.menu.menuCreate_guildTitle
            }
        }

        inventoryClick { item, clickContext ->
            context.icon = item.clone().apply {
                this.amount = 1
            }
            clickContext.stack.replace(createMenuPage(viewer, context))
            return@inventoryClick true
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

        3 to 2 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuCreate_guildButtonSet_id
                    }
                }
                if (context.id.isNotEmpty()) {
                    description {
                        newline()
                        mini("<italic:false><white>${context.id}")
                    }
                }
            }
            clickEvent { menuPageContext ->
                viewer.openDialog(
                    createGuild_setIdDialog(viewer, menuPageContext.menuContext, context)
                )
            }
        }

        3 to 5 eq {
            icon(ItemStack(Material.PAPER))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuCreate_guildButtonModify_display_name
                    }
                }
                if (context.displayName.isNotEmpty()) {
                    description {
                        newline()
                        localization(viewer) {
                            context.displayName
                        }
                    }
                }
            }
            clickEvent { menuPageContext ->
                viewer.openDialog(
                    createGuild_modifyDisplayName(viewer, menuPageContext.menuContext, context)
                )
            }
        }

        3 to 8 eq {
            icon(context.icon)
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuCreate_guildButtonModify_icon
                    }
                }
                description {
                    newline()
                    localization(viewer) {
                        "<italic:false><white>${this.guild.menu.menuCreate_guildButtonModify_iconTooltip}"
                    }
                }
            }
            clickEvent {

            }
        }
        5 to 9 eq {
            icon(ItemStack(Material.EMERALD))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuCreate_guildButtonCreate
                    }
                }
            }
            clickEvent { menuPageContext ->
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
                menuPageContext.stack.replace(guildView(viewer, guild, mutableListOf()) {
                    // DO NOTHING BECAUSE NO NEED TO RERENDER
                })
            }
        }
    }
}