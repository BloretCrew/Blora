package blora.menu

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt

class SimpleMenuPage(
    val title: (ComponentKt.() -> Unit)? = null,
    val lines: Int,
    val items: Map<Int, MenuItemBuilder.() -> Unit>,
    val playerInventoryClickHandler: (ItemStack, MenuContext) -> Boolean = { item, context -> false },
) : MenuPage<SimpleMenuPageContext>() {

    override fun fireClickEvent(clickIndex: Int, menu: MenuContext) {
        if (items.containsKey(clickIndex)) {
            val menuItem = MenuItemBuilder().apply(items[clickIndex]!!).build()
            val clickEvent = menuItem.clickEvent
            if (clickEvent != null) {
                clickEvent(SimpleMenuPageContext(menu))
            }
        }
    }

    override fun firePlayerClickEvent(itemStack: ItemStack, menu: MenuContext): Boolean {
        return this.playerInventoryClickHandler(itemStack, menu)
    }

    override fun render(context: Menu, inventory: Inventory) {
        if (this.title != null) {
            context.updateTitle(component(this.title))
        }
        for ((index, builder) in this.items) {
            val menuItem = MenuItemBuilder().apply(builder).build()
            inventory.setItem(index, menuItem.icon.apply {
                if (this == null || this.isEmpty || this.type.isAir || !this.type.isItem) {
                    return
                }

                if (!menuItem.useItemInfoAsHover) {
                    val hoverTextSupplier = menuItem.hoverText

                    val hoverText = hoverTextSupplier(context)
                    val title = hoverText.title
                    if (title != null) {
                        this.setData(DataComponentTypes.CUSTOM_NAME, title)
                    }
                    val lore = hoverText.description
                    if (lore != null) {
                        this.setData(DataComponentTypes.LORE, lore)
                    }
                }
            })
        }
    }

}

class SimpleMenuPageContext(override val menuContext: MenuContext) : MenuPageContext() {

    override val menu: Menu
        get() = menuContext.menu
    override val clickType: ClickType
        get() = menuContext.clickType
    override val inventoryAction: InventoryAction
        get() = menuContext.inventoryAction

}