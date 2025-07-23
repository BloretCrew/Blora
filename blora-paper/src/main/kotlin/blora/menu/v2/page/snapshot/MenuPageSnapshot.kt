package blora.menu.v2.page.snapshot

import blora.menu.v2.Menu
import blora.menu.v2.context.MenuClickContext
import blora.menu.v2.context.MenuViewContext
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

interface MenuPageSnapshot<C : MenuClickContext, V : MenuViewContext> {

    fun createContext(menu: Menu, event: InventoryClickEvent): C

    fun fireMenuClickEvent(clickIndex: Int, menu: Menu, event: InventoryClickEvent)
    fun firePlayerInventoryClickEvent(itemStack: ItemStack, menu: Menu, event: InventoryClickEvent): Boolean

}