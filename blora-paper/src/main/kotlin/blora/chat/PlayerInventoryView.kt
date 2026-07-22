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
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.util.*

/**
 * Opens a read-only snapshot of another player's inventory / ender chest.
 * Snapshots are taken at open time (no live binding) to avoid item-dupe edge cases.
 */
object PlayerInventoryView : Listener {

    private val views: MutableMap<UUID, MutableList<Menu>> = mutableMapOf()
    private val enderChestViews: MutableMap<UUID, MutableList<Menu>> = mutableMapOf()

    fun viewEnderChest(viewer: Player, viewee: Player) {
        val snapshot = Array(27) { index ->
            viewee.enderChest.getItem(index)?.clone()
        }
        val menu = Menu(
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
            { closed ->
                // Match Menu lifecycle: unregister listener; inventory is already closing.
                closed.destroy()
                enderChestViews[viewee.uniqueId]?.remove(closed)
                if (enderChestViews[viewee.uniqueId]?.isEmpty() == true) {
                    enderChestViews.remove(viewee.uniqueId)
                }
            },
            menuPage {
                lines(3)
                for (row in 0 until 3) {
                    for (column in 0 until 9) {
                        val item = snapshot[row * 9 + column]
                        if (item == null || item.isEmpty)
                            continue
                        (row + 1) to (column + 1) eq {
                            icon(item)
                            useItemInfoAsHover()
                        }
                    }
                }
            }
        )
        enderChestViews.getOrPut(viewee.uniqueId) { mutableListOf() }.add(menu)
        menu.open()
    }

    fun view(viewer: Player, viewee: Player) {
        val hotbar = Array(9) { index ->
            viewee.inventory.getItem(index)?.clone()
        }
        val storage = Array(27) { index ->
            // slots 9..35
            viewee.inventory.getItem(index + 9)?.clone()
        }
        val helmet = viewee.inventory.helmet?.clone()
        val chestplate = viewee.inventory.chestplate?.clone()
        val leggings = viewee.inventory.leggings?.clone()
        val boots = viewee.inventory.boots?.clone()
        val offhand = viewee.inventory.itemInOffHand.clone()
        val profile = viewee.playerProfile

        val menu = Menu(
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
            { closed ->
                closed.destroy()
                views[viewee.uniqueId]?.remove(closed)
                if (views[viewee.uniqueId]?.isEmpty() == true) {
                    views.remove(viewee.uniqueId)
                }
            },
            menuPage {
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
                                    ResolvableProfile.resolvableProfile(profile)
                                )
                            }
                    )
                }

                for (i in 0 until 9) {
                    val item = hotbar[i]
                    if (item == null || item.isEmpty)
                        continue
                    6 to (i + 1) eq {
                        icon(item)
                        useItemInfoAsHover()
                    }
                }

                for (row in 0 until 3) {
                    for (column in 0 until 9) {
                        val item = storage[row * 9 + column]
                        if (item == null || item.isEmpty)
                            continue
                        (row + 3) to (column + 1) eq {
                            icon(item)
                            useItemInfoAsHover()
                        }
                    }
                }

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
                if (!offhand.isEmpty) {
                    1 to 8 eq {
                        icon(offhand)
                        useItemInfoAsHover()
                    }
                }
            }
        )
        views.getOrPut(viewee.uniqueId) { mutableListOf() }.add(menu)
        menu.open()
    }

    fun stopJob() {
        views.values.flatten().forEach(Menu::destroy)
        enderChestViews.values.flatten().forEach(Menu::destroy)
        views.clear()
        enderChestViews.clear()
    }

    fun startJob() {
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        views.remove(event.player.uniqueId)?.forEach(Menu::destroy)
        enderChestViews.remove(event.player.uniqueId)?.forEach(Menu::destroy)
    }

}
