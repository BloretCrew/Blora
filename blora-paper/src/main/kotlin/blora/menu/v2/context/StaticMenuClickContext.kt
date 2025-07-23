package blora.menu.v2.context

import blora.menu.v2.Menu
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction

class StaticMenuClickContext(
    menu: Menu,
    click: ClickType,
    action: InventoryAction
) : AbstractMenuClickContext(menu, click, action) {
}