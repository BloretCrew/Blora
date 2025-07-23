package blora.menu

import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.text.replacements

// TODO
class PageableMenuPage<T>(
    val title: Component? = null,
    val lines: Int,
    val items: MutableList<T>,
    val itemBuilder: MenuItemBuilder.(T) -> Unit,
    val playerInventoryClickHandler: (ItemStack, MenuContext) -> Boolean = { item, context -> false },
) : MenuPage<PageableMenuPageContext<T>>() {

    private var currentPage: Int = 1

    override fun fireClickEvent(clickIndex: Int, menu: MenuContext) {
        TODO("Not yet implemented")
    }

    override fun firePlayerClickEvent(
        itemStack: ItemStack,
        menu: MenuContext
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun render(context: Menu, inventory: Inventory) {
        menuPage {
            lines(this.lines)
            title {
                replacements {
                    replacement {
                        matchLiteral("<current>")
                    }
                }
            }
        }
    }

}

class PageableMenuPageContext<T>(
    val items: MutableList<T>,
    override val menuContext: MenuContext
) : MenuPageContext() {

    override val menu: Menu
        get() = menuContext.menu
    override val clickType: ClickType
        get() = menuContext.clickType
    override val inventoryAction: InventoryAction
        get() = menuContext.inventoryAction

}