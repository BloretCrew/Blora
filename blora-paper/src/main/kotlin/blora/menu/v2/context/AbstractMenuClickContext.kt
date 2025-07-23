package blora.menu.v2.context

import blora.menu.v2.Menu
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction

abstract class AbstractMenuClickContext(
    override val menu: Menu,
    override val click: ClickType,
    override val action: InventoryAction
) : MenuClickContext