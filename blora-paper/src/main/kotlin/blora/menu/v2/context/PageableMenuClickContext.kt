package blora.menu.v2.context

import blora.menu.v2.Menu
import blora.menu.v2.page.PageableMenuPage
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction

class PageableMenuClickContext<D>(
    menu: Menu,
    click: ClickType,
    action: InventoryAction,
    val menuPage: PageableMenuPage<D>,
    val currentPage: Int,
    val maxPage: Int
) : AbstractMenuClickContext(menu, click, action)