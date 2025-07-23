package blora.menu.v2.page

import blora.menu.v2.Menu
import blora.menu.v2.context.StaticMenuClickContext
import blora.menu.v2.context.StaticMenuViewContext
import blora.menu.v2.item.MenuItemSnapshot
import blora.menu.v2.page.snapshot.MenuPageSnapshot
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class StaticMenuPage(
    val title: Component?,
    val items: Map<Int, MenuItemSnapshot<StaticMenuClickContext>>,
    val playerInventoryClickHandler: (ItemStack, StaticMenuClickContext) -> Boolean,
    private val launchEffect: (StaticMenuViewContext) -> Unit,
    private val disposeEffect: (StaticMenuViewContext) -> Unit,
) : MenuPage<StaticMenuClickContext, StaticMenuViewContext>,
    MenuPageSnapshot<StaticMenuClickContext, StaticMenuViewContext> { // as a static page, snapshot is itself

    override fun fireLaunchEffect(menu: Menu) {
        this.launchEffect(StaticMenuViewContext(menu))
    }

    override fun fireDisposeEffect(menu: Menu) {
        this.disposeEffect(StaticMenuViewContext(menu))
    }

    override fun render(
        menu: Menu,
        inventory: Inventory
    ): MenuPageSnapshot<out StaticMenuClickContext, out StaticMenuViewContext> {
        if (this.title != null) {
            menu.updateTitle(this.title)
        }
        for ((index, item) in this.items) {
            inventory.setItem(index, item.icon)
        }
        return this
    }

    override fun createContext(
        menu: Menu,
        event: InventoryClickEvent
    ): StaticMenuClickContext {
        return StaticMenuClickContext(
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

    override fun firePlayerInventoryClickEvent(itemStack: ItemStack, menu: Menu, event: InventoryClickEvent): Boolean {
        return this.playerInventoryClickHandler.invoke(itemStack, this.createContext(menu, event))
    }

}