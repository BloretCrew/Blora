package blora.menu.v2.page.snapshot

import blora.menu.v2.Menu
import blora.menu.v2.context.PageableMenuClickContext
import blora.menu.v2.context.PageableMenuViewContext
import blora.menu.v2.item.MenuItemSnapshot
import blora.menu.v2.page.PageableMenuPage
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class PageableDynamicMenuPageSnapshot<D>(
    val menuPage: PageableMenuPage<D>,
    val currentPage: Int,
    val maxPage: Int,
    val title: Component?,
    val items: Map<Int, MenuItemSnapshot<PageableMenuClickContext<D>>>,
    val playerInventoryClickHandler: (ItemStack, PageableMenuClickContext<D>) -> Boolean
) : MenuPageSnapshot<PageableMenuClickContext<D>, PageableMenuViewContext<D>> {

    override fun createContext(
        menu: Menu,
        event: InventoryClickEvent
    ): PageableMenuClickContext<D> {
        return PageableMenuClickContext(
            menu,
            event.click,
            event.action,
            this.menuPage.builder.dataProvider,
            this.menuPage,
            this.currentPage,
            this.maxPage
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
        menu: Menu,
        event: InventoryClickEvent
    ): Boolean {
        return this.playerInventoryClickHandler.invoke(itemStack, this.createContext(menu, event))
    }

}