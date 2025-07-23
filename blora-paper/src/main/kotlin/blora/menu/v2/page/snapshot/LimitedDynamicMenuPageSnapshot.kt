package blora.menu.v2.page.snapshot

import blora.menu.v2.Menu
import blora.menu.v2.context.LimitedDynamicMenuClickContext
import blora.menu.v2.context.LimitedDynamicMenuViewContext
import blora.menu.v2.item.MenuItemSnapshot
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class LimitedDynamicMenuPageSnapshot(
    val title: Component?,
    val items: Map<Int, MenuItemSnapshot<LimitedDynamicMenuClickContext>>,
    val playerInventoryClickHandler: (ItemStack, LimitedDynamicMenuClickContext) -> Boolean
) : MenuPageSnapshot<LimitedDynamicMenuClickContext, LimitedDynamicMenuViewContext> {

    override fun createContext(
        menu: Menu,
        event: InventoryClickEvent
    ): LimitedDynamicMenuClickContext {
        return LimitedDynamicMenuClickContext(
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