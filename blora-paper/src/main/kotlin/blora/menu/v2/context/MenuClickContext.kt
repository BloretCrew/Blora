package blora.menu.v2.context

import blora.menu.v2.MenuStack
import blora.menu.v2.Menu
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction

interface MenuClickContext {

    val menu: Menu
    val click: ClickType
    val action: InventoryAction
    val stack: MenuStack
        get() = this.menu.stack
    val viewer: Player
        get() = this.menu.viewer

}