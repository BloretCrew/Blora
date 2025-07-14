package blora.menu

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt
import kotlin.math.min

class MenuPage(
    val title: Component? = null,
    val lines: Int,
    val items: Map<Int, MenuItem>,
    val playerInventoryClickHandler: (ItemStack, MenuContext) -> Boolean = { item, context -> false },
) {

    fun fireClickEvent(clickIndex: Int, menu: MenuContext) {
        if (items.containsKey(clickIndex)) {
            val clickEvent = items[clickIndex]!!.clickEvent
            if (clickEvent != null) {
                clickEvent(menu)
            }
        }
    }

    fun firePlayerClickEvent(itemStack: ItemStack, menu: MenuContext): Boolean {
        return this.playerInventoryClickHandler(itemStack, menu)
    }

    fun render(context: Menu, inventory: Inventory) {
        if (this.title != null) {
            context.updateTitle(this.title)
        }
        for ((index, menuItem) in this.items) {
            inventory.setItem(index, menuItem.icon.apply {
                if (this == null || this.isEmpty || this.type.isAir || !this.type.isItem) {
                    return
                }

                val hoverTextSupplier = menuItem.hoverText

                if (hoverTextSupplier != null) {
                    val hoverText = hoverTextSupplier(context)
                    val title = hoverText.title
                    if (title != null) {
                        this.setData(DataComponentTypes.ITEM_NAME, title)
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

class MenuPageBuilder {

    internal var title: Component? = null
    internal var lines = 6
    internal val items: MutableMap<Int, MenuItem> = mutableMapOf()
    internal val mapItems: MutableMap<Char, MenuItem> = mutableMapOf()
    internal var map: List<String> = listOf()
    internal var playerInventoryClickHandler: (ItemStack, MenuContext) -> Boolean = { item, context -> false }

    infix fun Pair<Int, Int>.eq(item: MenuItem) {
        items[(this.second - 1) + ((this.first - 1) * 9)] = item
    }

    infix fun Pair<Int, Int>.eq(builder: MenuItemBuilder.() -> Unit) {
        items[(this.second - 1) + ((this.first - 1) * 9)] = menuItem(builder)
    }

    infix fun Char.eq(item: MenuItem) {
        if (this == ' ')
            throw IllegalArgumentException("map key cannot be white space")
        mapItems[this] = item
    }

    infix fun Char.eq(builder: MenuItemBuilder.() -> Unit) {
        if (this == ' ')
            throw IllegalArgumentException("map key cannot be white space")
        mapItems[this] = menuItem(builder)
    }

    fun build(): MenuPage {
        val finalItems = mutableMapOf<Int, MenuItem>()
        for (rowIndex in (0 until min(this.lines, this.map.size))) {
            val row = this.map[rowIndex]
            for (columnIndex in (0 until min(9, row.length))) {
                val column = row[columnIndex]
                if (column == ' ')
                    continue
                val menuItem = mapItems[column]
                if (menuItem == null)
                    continue
                finalItems[rowIndex * 9 + columnIndex] = menuItem
            }
        }
        for ((index, item) in this.items) {
            finalItems[index] = item
        }
        return MenuPage(this.title, this.lines, finalItems.toMap(), this.playerInventoryClickHandler)
    }

}

fun MenuPageBuilder.title(builder: ComponentKt.() -> Unit) {
    this.title = component(builder)
}

fun MenuPageBuilder.lines(lines: Int) {
    require(lines >= 1 && lines <= 6)
    this.lines = lines
}

fun MenuPageBuilder.mapping(vararg lines: String) {
    this.map = lines.toList()
}

fun MenuPageBuilder.inventoryClick(handler: (ItemStack, MenuContext) -> Boolean) {
    this.playerInventoryClickHandler = handler
}

fun menuPage(builder: MenuPageBuilder.() -> Unit): MenuPage {
    return MenuPageBuilder().apply(builder).build()
}