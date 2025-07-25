@file:Suppress("UnstableApiUsage")

package blora.menu.v2.page.builder

import blora.item.clone
import blora.item.itemStack
import blora.menu.v2.Menu
import blora.menu.v2.context.StaticMenuClickContext
import blora.menu.v2.context.StaticMenuViewContext
import blora.menu.v2.item.MenuItemBuilder
import blora.menu.v2.item.MenuItemSnapshot
import blora.menu.v2.page.StaticMenuPage
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import kotlin.math.min

class StaticMenuPageBuilder : MenuPageBuilder<StaticMenuClickContext, StaticMenuViewContext, StaticMenuPage>() {

    override fun build(menu: Menu): StaticMenuPage {
        val context = StaticMenuViewContext(menu)
        val finalItems = mutableMapOf<Int, MenuItemSnapshot<StaticMenuClickContext>>()
        for (rowIndex in (0 until min(menu.lines, this.map.size))) {
            val row = this.map[rowIndex]
            for (columnIndex in (0 until min(9, row.length))) {
                val column = row[columnIndex]
                if (column == ' ')
                    continue
                val menuItemBuilder = mapItems[column]
                if (menuItemBuilder == null)
                    continue
                val menuItem = MenuItemBuilder<StaticMenuClickContext>()
                menuItem.menuItemBuilder(context)
                finalItems[rowIndex * 9 + columnIndex] = MenuItemSnapshot(
                    itemStack {
                        clone { menuItem.icon }
                        if (menuItem.icon == null || menuItem.icon!!.isEmpty || menuItem.icon!!.type.isAir || !menuItem.icon!!.type.isItem) {
                            return@itemStack
                        }
                        if (!menuItem.useItemInfoAsHover) {
                            DataComponentTypes.CUSTOM_NAME eq (menuItem.name ?: Component.text(" "))
                            if (menuItem.description != null) {
                                DataComponentTypes.LORE eq menuItem.description!!
                            }
                        }
                    },
                    menuItem.clickEvent
                )
            }
        }
        for ((index, itemBuilder) in this.items) {
            val menuItem = MenuItemBuilder<StaticMenuClickContext>()
            menuItem.itemBuilder(context)
            finalItems[index] = MenuItemSnapshot(
                itemStack {
                    clone { menuItem.icon }
                    if (menuItem.icon == null || menuItem.icon!!.isEmpty || menuItem.icon!!.type.isAir || !menuItem.icon!!.type.isItem) {
                        return@itemStack
                    }
                    if (!menuItem.useItemInfoAsHover) {
                        DataComponentTypes.CUSTOM_NAME eq (menuItem.name ?: Component.text(" "))
                        if (menuItem.description != null) {
                            DataComponentTypes.LORE eq menuItem.description!!
                        }
                    }
                },
                menuItem.clickEvent
            )
        }
        for (itemBuilder in this.toBeAppend) {
            for (i in 0 until menu.lines * 9) {
                if (finalItems.containsKey(i))
                    continue
                val menuItem = MenuItemBuilder<StaticMenuClickContext>()
                menuItem.itemBuilder(context)
                finalItems[i] = MenuItemSnapshot(
                    itemStack {
                        clone { menuItem.icon }
                        if (menuItem.icon == null || menuItem.icon!!.isEmpty || menuItem.icon!!.type.isAir || !menuItem.icon!!.type.isItem) {
                            return@itemStack
                        }
                        if (!menuItem.useItemInfoAsHover) {
                            DataComponentTypes.CUSTOM_NAME eq (menuItem.name ?: Component.text(" "))
                            if (menuItem.description != null) {
                                DataComponentTypes.LORE eq menuItem.description!!
                            }
                        }
                    },
                    menuItem.clickEvent
                )
            }
        }
        return StaticMenuPage(
            this.pageId,
            if (this.title != null) component { title!!(context) } else null,
            finalItems,
            this.playerInventoryClickHandler,
            this.launchEffect,
            this.disposeEffect
        )
    }

}

fun staticMenuPage(menu: Menu, builder: StaticMenuPageBuilder.() -> Unit): StaticMenuPage {
    return StaticMenuPageBuilder().apply(builder).build(menu)
}