package blora.menu.v2.page

import blora.menu.v2.Menu
import blora.menu.v2.page.snapshot.MenuPageSnapshot
import blora.menu.v2.context.MenuClickContext
import blora.menu.v2.context.MenuViewContext
import org.bukkit.inventory.Inventory

interface MenuPage<C : MenuClickContext, V : MenuViewContext> {

    fun fireLaunchEffect(menu: Menu)
    fun fireDisposeEffect(menu: Menu)
    fun render(menu: Menu, inventory: Inventory): MenuPageSnapshot<out C, out V>

}