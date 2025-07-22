@file:Suppress("DEPRECATION")

package blora.menu

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
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class Menu(
    val contextObject: Any?,
    val viewer: Player,
    val lines: Int,
    val title: Component,
    val closer: (Menu) -> Unit,
    val basePage: MenuPage<*>
) : Listener, InventoryHolder {

    private val extraClosingHooks: MutableList<(Menu) -> Unit> = mutableListOf()

    constructor(
        contextObject: Any?,
        viewer: Player,
        lines: Int,
        title: Component,
        closer: (Menu) -> Unit,
        builder: MenuPageBuilder.() -> Unit
    ) : this(
        contextObject,
        viewer,
        lines,
        title,
        closer,
        menuPage(builder)
    )

    private val inventory: Inventory
    val stack: MenuStack = MenuStack(this, this.lines, this.basePage)

    init {
        require(lines >= 1 && lines <= 6)
        this.inventory = Bukkit.createInventory(this, this.lines * 9, this.title)
        this.update(this.basePage)
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
    }

    fun hookClosing(handler: (Menu) -> Unit) {
        this.extraClosingHooks.add(handler)
    }

    fun open() {
        this.viewer.openInventory(this.inventory)
        this.rerender()
    }

    fun rerender() {
        this.inventory.clear()
        stack.current().render(this, this.inventory)
    }

    fun update(page: MenuPage<*>) {
        this.inventory.clear()
        page.render(this, this.inventory)
    }

    fun destroy() {
        this.inventory.clear() // prevent bug if some plugin cancel player closing inventory
        HandlerList.unregisterAll(this) // unregister to prevent trigger closer
        this.viewer.closeInventory()
    }

    fun updateTitle(component: Component) {
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

    @EventHandler
    fun clickHandler(event: InventoryClickEvent) {
        if (event.inventory.holder != this)
            return
        if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.isCancelled = true
            return
        }
        if (event.clickedInventory != event.view.topInventory) {
            if (event.currentItem == null)
                return

            event.isCancelled = this.stack.current().firePlayerClickEvent(
                event.currentItem!!,
                MenuContext(
                    this,
                    event.click,
                    event.action
                )
            )
            return
        }
        event.isCancelled = true

        // run delay 1 tick to prevent failed to set cursor item
        Bukkit.getScheduler().runTaskLater(
            BloraPlugin,
            Runnable {
                this.stack.current().fireClickEvent(
                    event.rawSlot,
                    MenuContext(
                        this,
                        event.click,
                        event.action
                    )
                )
            },
            1L
        )
    }

    @EventHandler
    fun closeHandler(event: InventoryCloseEvent) {
        if (event.inventory == this.inventory) {
            this.closer.invoke(this)
            this.extraClosingHooks.forEach { it(this) }
        }
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