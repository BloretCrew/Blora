package blora.menu.storage

import blora.plugin.BloraPlugin
import blora.scheduler.ShutdownHook
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftContainer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

class StorageMenu(
    val identity: Any, initial: Map<Int, ItemStack>,
    val lines: Int,
    val updateFrequency: Long,
    private val removal: (StorageMenu) -> Unit,
    private val updater: (Map<Int, ItemStack>) -> Unit
) : InventoryHolder, Listener {

    private val inventory: Inventory
    private val updateJob: BukkitTask
    private val shutdownHook: ShutdownHook = ShutdownHook {
        this.inventory.close()
        this.triggerUpdater()
        this.inventory.clear()
        this.removal(this)
        HandlerList.unregisterAll(this)
    }

    private var startingRemoval: Boolean = false

    private var lastTimeHasViewers = System.currentTimeMillis()

    init {
        require(lines >= 1 && lines <= 6)
        require(this.updateFrequency > 0)
        this.inventory = Bukkit.createInventory(this, this.lines * 9, Component.text(" "))
        for (index in 0 until lines * 9) {
            val item = initial[index]
            if (item == null)
                continue
            this.inventory.setItem(index, item)
        }
        this.updateJob = Bukkit.getScheduler().runTaskTimer(
            BloraPlugin,
            Runnable {
                this.triggerUpdater()
                if (this.inventory.viewers.isNotEmpty())
                    this.lastTimeHasViewers = System.currentTimeMillis()
                // drop this menu if 5 minutes no viewer using
                if ((System.currentTimeMillis() - this.lastTimeHasViewers) > 5 * 60L * 1000L) {
                    this.startingRemoval = true
                    this.inventory.close() // prevent open when removing
                    this.triggerUpdater()
                    this.inventory.clear()
                    this.removal(this)
                    HandlerList.unregisterAll(this)
                }
            },
            this.updateFrequency,
            this.updateFrequency
        )
        BloraPlugin.hookShutdown(this.shutdownHook)
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
    }

    fun openForPlayer(player: Player, title: Component) {
        if (this.startingRemoval)
            return
        player.openInventory(this.inventory)
        val containerId = ((player) as CraftPlayer).handle.containerMenu.containerId
        val windowType = CraftContainer.getNotchInventoryType(this.inventory)
        player.handle.connection.send(
            ClientboundOpenScreenPacket(
                containerId,
                windowType,
                PaperAdventure.asVanilla(title)
            )
        )
        player.updateInventory()
    }

    private fun triggerUpdater() {
        val items = mutableMapOf<Int, ItemStack>()
        for (index in 0 until lines * 9) {
            val item = inventory.getItem(index)
            if (item == null)
                continue
            if (item.isEmpty)
                continue
            items[index] = item.clone()
        }
        this.updater(items)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.holder != this)
            return
        this.triggerUpdater()
    }

    override fun getInventory(): Inventory {
        return this.inventory
    }

}