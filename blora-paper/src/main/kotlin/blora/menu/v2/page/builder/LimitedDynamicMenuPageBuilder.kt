package blora.menu.v2.page.builder

import blora.menu.v2.Menu
import blora.menu.v2.context.LimitedDynamicMenuClickContext
import blora.menu.v2.context.LimitedDynamicMenuViewContext
import blora.menu.v2.item.MenuItemBuilder
import blora.menu.v2.page.LimitedDynamicMenuPage
import kotlin.math.min

class LimitedDynamicMenuPageBuilder
    : MenuPageBuilder<LimitedDynamicMenuClickContext, LimitedDynamicMenuViewContext, LimitedDynamicMenuPage>() {

    override fun build(menu: Menu): LimitedDynamicMenuPage {
        val finalItems =
            mutableMapOf<Int, MenuItemBuilder<LimitedDynamicMenuClickContext>.(LimitedDynamicMenuViewContext) -> Unit>()
        for (rowIndex in (0 until min(menu.lines, this.map.size))) {
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
            for (i in 0 until menu.lines * 9) {
                if (finalItems.containsKey(i))
                    continue
                finalItems[i] = item
            }
        }
        return LimitedDynamicMenuPage(
            this.title,
            finalItems,
            this.playerInventoryClickHandler,
            this.launchEffect,
            this.disposeEffect
        )
    }

}

fun limitedDynamicMenuPage(menu: Menu, builder: LimitedDynamicMenuPageBuilder.() -> Unit): LimitedDynamicMenuPage {
    return LimitedDynamicMenuPageBuilder().apply(builder).build(menu)
}