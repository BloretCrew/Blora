@file:Suppress("UnstableApiUsage")

package blora.chat

import blora.extension.localization
import blora.menu.*
import blora.plugin.BloraPlugin
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.util.*

object PlayerInventoryView : Listener {

    private val views: MutableMap<UUID, MutableList<Menu>> = mutableMapOf()
    private val enderChestViews: MutableMap<UUID, MutableList<Menu>> = mutableMapOf()

    private lateinit var job: BukkitTask

    fun viewEnderChest(viewer: Player, viewee: Player) {
        this.enderChestViews.getOrPut(viewee.uniqueId) { mutableListOf() }
            .add(
                Menu(
                    null,
                    viewer,
                    3,
                    component {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("viewee", viewee.name)
                            }
                        ) {
                            this.chatViewEnderChest
                        }
                    },
                    {
                        it.destroy()
                        views.getOrPut(viewee.uniqueId) { mutableListOf() }.remove(it)
                    },
                    menuPage { lines(3) }
                ).apply {
                    this.open()
                }
            )
    }

    fun view(viewer: Player, viewee: Player) {
        this.views.getOrPut(viewee.uniqueId) { mutableListOf() }
            .add(
                Menu(
                    null,
                    viewer,
                    6,
                    component {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("viewee", viewee.name)
                            }
                        ) {
                            this.chatViewInventory
                        }
                    },
                    {
                        it.destroy()
                        views.getOrPut(viewee.uniqueId) { mutableListOf() }.remove(it)
                    },
                    menuPage { lines(6) }
                ).apply {
                    this.open()
                }
            )
    }

    fun stopJob() {
        job.cancel()
        this.views.values.flatten().forEach(Menu::destroy)
    }

    fun startJob() {
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
        Bukkit.getScheduler().runTaskTimer(
            BloraPlugin,
            { task ->
                this@PlayerInventoryView.job = task
                for ((uuid, menus) in views) {
                    val player = Bukkit.getPlayer(uuid)
                    if (player == null)
                        continue
                    if (!player.isOnline)
                        continue
                    val menuPage = menuPage {
                        lines(6)
                        mapping(
                            "H#    # #",
                            "#########"
                        )
                        '#' eq {
                            icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
                        }
                        'H' eq {
                            icon(
                                ItemStack(Material.PLAYER_HEAD)
                                    .apply {
                                        this.setData(
                                            DataComponentTypes.PROFILE,
                                            ResolvableProfile.resolvableProfile(player.playerProfile)
                                        )
                                    }
                            )
                        }

                        // hotbar items
                        for (i in 0 until 9) {
                            val item = player.inventory.getItem(i)
                            if (item == null)
                                continue
                            if (item.isEmpty)
                                continue
                            6 to (i + 1) eq {
                                icon(item)
                                useItemInfoAsHover()
                            }
                        }

                        // inventory items
                        for (row in 1..3) {
                            for (column in 0 until 9) {
                                val item = player.inventory.getItem(row * 9 + column)
                                if (item == null)
                                    continue
                                if (item.isEmpty)
                                    continue
                                (row + 2) to (column + 1) eq {
                                    icon(item)
                                    useItemInfoAsHover()
                                }
                            }
                        }

                        // armors
                        val helmet = player.inventory.helmet
                        val chestplate = player.inventory.chestplate
                        val leggings = player.inventory.leggings
                        val boots = player.inventory.boots

                        if (helmet != null && !helmet.isEmpty) {
                            1 to 3 eq {
                                icon(helmet)
                                useItemInfoAsHover()
                            }
                        }
                        if (chestplate != null && !chestplate.isEmpty) {
                            1 to 4 eq {
                                icon(chestplate)
                                useItemInfoAsHover()
                            }
                        }
                        if (leggings != null && !leggings.isEmpty) {
                            1 to 5 eq {
                                icon(leggings)
                                useItemInfoAsHover()
                            }
                        }
                        if (boots != null && !boots.isEmpty) {
                            1 to 6 eq {
                                icon(boots)
                                useItemInfoAsHover()
                            }
                        }

                        val offhand = player.inventory.itemInOffHand
                        if (!offhand.isEmpty) {
                            1 to 8 eq {
                                icon(offhand)
                                useItemInfoAsHover()
                            }
                        }
                    }
                    menus.forEach { it.stack.replace(menuPage) }
                }
                for ((uuid, menus) in enderChestViews) {
                    val player = Bukkit.getPlayer(uuid)
                    if (player == null)
                        continue
                    if (!player.isOnline)
                        continue
                    val menuPage = menuPage {
                        lines(3)

                        for (row in 0 until 3) {
                            for (column in 0 until 9) {
                                val item = player.enderChest.getItem(row * 9 + column)
                                if (item == null)
                                    continue
                                if (item.isEmpty)
                                    continue
                                (row + 1) to (column + 1) eq {
                                    icon(item)
                                    useItemInfoAsHover()
                                }
                            }
                        }
                    }
                    menus.forEach { it.stack.replace(menuPage) }
                }
            },
            0L,
            5L
        )
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        views[event.player.uniqueId]?.forEach(Menu::destroy)
        enderChestViews[event.player.uniqueId]?.forEach(Menu::destroy)
    }

}
