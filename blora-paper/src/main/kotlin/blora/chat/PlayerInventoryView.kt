@file:Suppress("UnstableApiUsage")

package blora.chat

import blora.extension.localization
import blora.menu.*
import blora.plugin.BloraPlugin
import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Frozen inventory / ender chest taken when the chat message is sent.
 * Opening later only shows this snapshot (not the player's live contents).
 */
data class InventorySnapshot(
    val vieweeName: String,
    val profile: PlayerProfile,
    val hotbar: Array<ItemStack?>,
    val storage: Array<ItemStack?>,
    val helmet: ItemStack?,
    val chestplate: ItemStack?,
    val leggings: ItemStack?,
    val boots: ItemStack?,
    val offhand: ItemStack,
) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

data class EnderChestSnapshot(
    val vieweeName: String,
    val slots: Array<ItemStack?>,
) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

private data class TimedEntry<T>(
    val value: T,
    val expiresAtMs: Long,
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return expiresAtMs != Long.MAX_VALUE && now > expiresAtMs
    }
}

/**
 * Opens read-only menus from chat-time snapshots stored with a configurable TTL.
 */
object PlayerInventoryView : Listener {

    private val inventoryCache = ConcurrentHashMap<UUID, TimedEntry<InventorySnapshot>>()
    private val enderChestCache = ConcurrentHashMap<UUID, TimedEntry<EnderChestSnapshot>>()

    /** viewer UUID -> open menus (for cleanup on quit) */
    private val openMenus: MutableMap<UUID, MutableList<Menu>> = mutableMapOf()

    fun captureInventory(player: Player): InventorySnapshot {
        return InventorySnapshot(
            vieweeName = player.name,
            profile = player.playerProfile,
            hotbar = Array(9) { index -> player.inventory.getItem(index)?.clone() },
            storage = Array(27) { index -> player.inventory.getItem(index + 9)?.clone() },
            helmet = player.inventory.helmet?.clone(),
            chestplate = player.inventory.chestplate?.clone(),
            leggings = player.inventory.leggings?.clone(),
            boots = player.inventory.boots?.clone(),
            offhand = player.inventory.itemInOffHand.clone(),
        )
    }

    fun captureEnderChest(player: Player): EnderChestSnapshot {
        return EnderChestSnapshot(
            vieweeName = player.name,
            slots = Array(27) { index -> player.enderChest.getItem(index)?.clone() },
        )
    }

    /**
     * Store a snapshot for later click callbacks. Returns an id to embed in chat.
     */
    fun storeInventory(snapshot: InventorySnapshot): UUID {
        purgeExpired()
        val id = UUID.randomUUID()
        inventoryCache[id] = TimedEntry(snapshot, expiresAtFromConfig())
        return id
    }

    fun storeEnderChest(snapshot: EnderChestSnapshot): UUID {
        purgeExpired()
        val id = UUID.randomUUID()
        enderChestCache[id] = TimedEntry(snapshot, expiresAtFromConfig())
        return id
    }

    fun viewById(viewer: Player, snapshotId: UUID) {
        val entry = inventoryCache[snapshotId]
        if (entry == null || entry.isExpired()) {
            inventoryCache.remove(snapshotId)
            sendExpired(viewer)
            return
        }
        view(viewer, entry.value)
    }

    fun viewEnderChestById(viewer: Player, snapshotId: UUID) {
        val entry = enderChestCache[snapshotId]
        if (entry == null || entry.isExpired()) {
            enderChestCache.remove(snapshotId)
            sendExpired(viewer)
            return
        }
        viewEnderChest(viewer, entry.value)
    }

    private fun expiresAtFromConfig(): Long {
        val ttlSeconds = BloraPlugin.configuration.chat.inventorySnapshotTtlSeconds
        if (ttlSeconds <= 0L) {
            return Long.MAX_VALUE
        }
        return System.currentTimeMillis() + ttlSeconds * 1000L
    }

    private fun sendExpired(viewer: Player) {
        viewer.send {
            localization(viewer) {
                this.chatViewSnapshotExpired
            }
        }
    }

    private fun purgeExpired() {
        val now = System.currentTimeMillis()
        inventoryCache.entries.removeIf { it.value.isExpired(now) }
        enderChestCache.entries.removeIf { it.value.isExpired(now) }
    }

    fun viewEnderChest(viewer: Player, snapshot: EnderChestSnapshot) {
        val menu = Menu(
            null,
            viewer,
            3,
            component {
                localization(
                    player = viewer,
                    tags = {
                        parsedPlaceholder("viewee", snapshot.vieweeName)
                    }
                ) {
                    this.chatViewEnderChest
                }
            },
            { closed ->
                closed.destroy()
                openMenus[viewer.uniqueId]?.remove(closed)
                if (openMenus[viewer.uniqueId]?.isEmpty() == true) {
                    openMenus.remove(viewer.uniqueId)
                }
            },
            menuPage {
                lines(3)
                for (row in 0 until 3) {
                    for (column in 0 until 9) {
                        val item = snapshot.slots[row * 9 + column]
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
        openMenus.getOrPut(viewer.uniqueId) { mutableListOf() }.add(menu)
        menu.open()
    }

    fun view(viewer: Player, snapshot: InventorySnapshot) {
        val menu = Menu(
            null,
            viewer,
            6,
            component {
                localization(
                    player = viewer,
                    tags = {
                        parsedPlaceholder("viewee", snapshot.vieweeName)
                    }
                ) {
                    this.chatViewInventory
                }
            },
            { closed ->
                closed.destroy()
                openMenus[viewer.uniqueId]?.remove(closed)
                if (openMenus[viewer.uniqueId]?.isEmpty() == true) {
                    openMenus.remove(viewer.uniqueId)
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
                                    ResolvableProfile.resolvableProfile(snapshot.profile)
                                )
                            }
                    )
                }

                for (i in 0 until 9) {
                    val item = snapshot.hotbar[i]
                    if (item == null || item.isEmpty)
                        continue
                    6 to (i + 1) eq {
                        icon(item)
                        useItemInfoAsHover()
                    }
                }

                for (row in 0 until 3) {
                    for (column in 0 until 9) {
                        val item = snapshot.storage[row * 9 + column]
                        if (item == null || item.isEmpty)
                            continue
                        (row + 3) to (column + 1) eq {
                            icon(item)
                            useItemInfoAsHover()
                        }
                    }
                }

                val helmet = snapshot.helmet
                if (helmet != null && !helmet.isEmpty) {
                    1 to 3 eq {
                        icon(helmet)
                        useItemInfoAsHover()
                    }
                }
                val chestplate = snapshot.chestplate
                if (chestplate != null && !chestplate.isEmpty) {
                    1 to 4 eq {
                        icon(chestplate)
                        useItemInfoAsHover()
                    }
                }
                val leggings = snapshot.leggings
                if (leggings != null && !leggings.isEmpty) {
                    1 to 5 eq {
                        icon(leggings)
                        useItemInfoAsHover()
                    }
                }
                val boots = snapshot.boots
                if (boots != null && !boots.isEmpty) {
                    1 to 6 eq {
                        icon(boots)
                        useItemInfoAsHover()
                    }
                }
                if (!snapshot.offhand.isEmpty) {
                    1 to 8 eq {
                        icon(snapshot.offhand)
                        useItemInfoAsHover()
                    }
                }
            }
        )
        openMenus.getOrPut(viewer.uniqueId) { mutableListOf() }.add(menu)
        menu.open()
    }

    fun stopJob() {
        openMenus.values.flatten().forEach { runCatching { it.destroy() } }
        openMenus.clear()
        inventoryCache.clear()
        enderChestCache.clear()
    }

    fun startJob() {
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        openMenus.remove(event.player.uniqueId)?.forEach { runCatching { it.destroy() } }
    }

}
