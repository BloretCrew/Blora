package blora.menu.v2.page.snapshot

import blora.menu.v2.Menu
import blora.menu.v2.context.CompleteDynamicMenuClickContext
import blora.menu.v2.context.CompleteDynamicMenuViewContext
import blora.menu.v2.item.MenuItemSnapshot
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class CompleteDynamicMenuPageSnapshot(
    val title: Component?,
    val items: Map<Int, MenuItemSnapshot<CompleteDynamicMenuClickContext>>,
    val playerInventoryClickHandler: (ItemStack, CompleteDynamicMenuClickContext) -> Boolean
) : MenuPageSnapshot<CompleteDynamicMenuClickContext, CompleteDynamicMenuViewContext>{

    override fun createContext(
        menu: Menu,
        event: InventoryClickEvent
    ): CompleteDynamicMenuClickContext {
        return CompleteDynamicMenuClickContext(
            menu,
            event.click,
            event.action
        )
    }

    override fun fireMenuClickEvent(clickIndex: Int, menu: Menu, event: InventoryClickEvent) {
        val menuItem = items[clickIndex]
        if (menuItem != null) {
            val clickEvent = menuItem.click
            if (clickEvent != null) {
                clickEvent(this.createContext(menu, event))
            }
        }
    }

    override fun firePlayerInventoryClickEvent(
        itemStack: ItemStack,
        menu: Menu, event: InventoryClickEvent
    ): Boolean {
        return this.playerInventoryClickHandler.invoke(itemStack, this.createContext(menu, event))
    }

}