package blora.menu.v2

import blora.menu.v2.page.StaticMenuPage
import blora.menu.v2.page.snapshot.MenuPageSnapshot
import blora.plugin.BloraPlugin
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
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class Menu(
    val viewer: Player,
    val lines: Int,
    val clickCooldown: Long = 0L
) : Listener, InventoryHolder {

    internal val stack: MenuStack = MenuStack(this)

    private var lastClick = System.currentTimeMillis()
    private val inventory: Inventory
    private val closers: MutableList<(Menu) -> Unit> = mutableListOf()
    private var snapshot: MenuPageSnapshot<*, *> =
        StaticMenuPage("*_placeholder", Component.empty(), emptyMap(), { _, _ -> false }, {}, {})

    init {
        require(lines >= 1 && lines <= 6)
        this.inventory = Bukkit.createInventory(this, this.lines * 9, Component.empty())
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
    }

    internal fun updateTitle(component: Component) {
        if (this.viewer.openInventory.topInventory != this.inventory) {
            return
        }
        val containerId = ((this.viewer) as CraftPlayer).handle.containerMenu.containerId
        val windowType = CraftContainer.getNotchInventoryType(this.inventory)
        this.viewer.handle.connection.send(
            ClientboundOpenScreenPacket(
                containerId,
                windowType,
                PaperAdventure.asVanilla(component)
            )
        )
        this.viewer.updateInventory()
    }

    fun open(): Menu {
        this.viewer.openInventory(this.inventory)
        this.rerender()
        return this
    }

    fun rerender(): Menu {
        this.inventory.clear()
        val current = stack.currentOrNull()
        if (current != null) {
            this.snapshot = current.render(this, this.inventory)
        } else {
            this.snapshot = StaticMenuPage("*_placeholder", Component.empty(), emptyMap(), { _, _ -> false }, {}, {})
        }
        return this
    }

    fun destroy() {
        this.stack.clear()
        this.inventory.clear() // prevent bug if some plugin cancel player closing inventory
        HandlerList.unregisterAll(this) // unregister to prevent trigger closer
        this.viewer.closeInventory()
    }

    fun closer(closer: (Menu) -> Unit): Menu {
        this.closers.add(closer)
        return this
    }

    @EventHandler
    fun clickHandler(event: InventoryClickEvent) {
        if (event.inventory.holder != this)
            return
        if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.action == InventoryAction.COLLECT_TO_CURSOR) {
            event.isCancelled = true
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastClick <= clickCooldown) {
            event.isCancelled = true
            return
        }
        lastClick = now // only update click time if click is success
        if (event.clickedInventory != event.view.topInventory) {
            if (event.currentItem == null)
                return

            event.isCancelled = this.snapshot.firePlayerInventoryClickEvent(event.currentItem!!, this, event)
            return
        }
        event.isCancelled = true

        // run delay 1 tick to prevent failed to set cursor item
        Bukkit.getScheduler().runTaskLater(
            BloraPlugin,
            Runnable {
                this.snapshot.fireMenuClickEvent(
                    event.rawSlot,
                    this,
                    event
                )
            },
            1L
        )
    }

    @EventHandler
    fun dragHandler(event: InventoryDragEvent) {
        if (event.inventory.holder != this)
            return
        val topSize = event.view.topInventory.size
        if (event.rawSlots.any { it < topSize }) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun closeHandler(event: InventoryCloseEvent) {
        if (event.inventory.holder != this || event.inventory != this.inventory)
            return
        this.closers.forEach { it(this) }
    }

    @EventHandler
    fun quitHandler(event: PlayerQuitEvent) {
        if (event.player == this.viewer) {
            this.destroy()
        }
    }

    override fun getInventory(): Inventory {
        return this.inventory
    }

}