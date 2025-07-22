@file:Suppress("UnstableApiUsage")

package blora.menu

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt
import kotlin.math.min

abstract class MenuPage<C : MenuPageContext> {

    abstract fun fireClickEvent(clickIndex: Int, menu: MenuContext)
    abstract fun firePlayerClickEvent(itemStack: ItemStack, menu: MenuContext): Boolean
    abstract fun render(context: Menu, inventory: Inventory)

}

abstract class MenuPageContext {

    abstract val menuContext: MenuContext
    abstract val menu: Menu
    abstract val clickType: ClickType
    abstract val inventoryAction: InventoryAction
    val stack: MenuStack
        get() = this.menu.stack

}

class MenuPageBuilder {

    internal var title: (ComponentKt.() -> Unit)? = null
    internal var lines = 6
    internal val items: MutableMap<Int, MenuItemBuilder.() -> Unit> = mutableMapOf()
    internal val mapItems: MutableMap<Char, MenuItemBuilder.() -> Unit> = mutableMapOf()
    internal val toBeAppend = mutableListOf<MenuItemBuilder.() -> Unit>()
    internal var map: List<String> = listOf()
    internal var playerInventoryClickHandler: (ItemStack, MenuContext) -> Boolean = { item, context -> false }

    infix fun Pair<Int, Int>.eq(item: MenuItem) {
        items[(this.second - 1) + ((this.first - 1) * 9)] = { item }
    }

    infix fun Pair<Int, Int>.eq(builder: MenuItemBuilder.() -> Unit) {
        items[(this.second - 1) + ((this.first - 1) * 9)] = builder
    }

    infix fun Char.eq(item: MenuItem) {
        if (this == ' ')
            throw IllegalArgumentException("map key cannot be white space")
        mapItems[this] = { item }
    }

    infix fun Char.eq(builder: MenuItemBuilder.() -> Unit) {
        if (this == ' ')
            throw IllegalArgumentException("map key cannot be white space")
        mapItems[this] = builder
    }

    fun build(): SimpleMenuPage {
        val finalItems = mutableMapOf<Int, MenuItemBuilder.() -> Unit>()
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
        for (item in this.toBeAppend) {
            for (i in 0 until this.lines * 9) {
                if (finalItems.containsKey(i))
                    continue
                finalItems[i] = item
            }
        }
        return SimpleMenuPage(this.title, this.lines, finalItems.toMap(), this.playerInventoryClickHandler)
    }

}

fun MenuPageBuilder.title(builder: ComponentKt.() -> Unit) {
    this.title = builder
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

fun MenuPageBuilder.appendLastEmpty(builder: MenuItemBuilder.() -> Unit) {
    this.toBeAppend.add(builder)
}

fun menuPage(builder: MenuPageBuilder.() -> Unit): SimpleMenuPage {
    return MenuPageBuilder().apply(builder).build()
}