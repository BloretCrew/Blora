@file:Suppress("UnstableApiUsage")

package blora.menu.v2.page.builder

import blora.item.clone
import blora.item.itemStack
import blora.menu.v2.Menu
import blora.menu.v2.context.CompleteDynamicMenuClickContext
import blora.menu.v2.context.CompleteDynamicMenuViewContext
import blora.menu.v2.item.MenuItemBuilder
import blora.menu.v2.item.MenuItemSnapshot
import blora.menu.v2.page.CompleteDynamicMenuPage
import blora.menu.v2.page.snapshot.CompleteDynamicMenuPageSnapshot
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import plutoproject.adventurekt.component
import kotlin.math.min

class CompleteDynamicMenuPageBuilder
    : MenuPageBuilder<CompleteDynamicMenuClickContext, CompleteDynamicMenuViewContext, CompleteDynamicMenuPage>() {

    internal var builder: CompleteDynamicMenuPageBuilder.() -> Unit = {}

    internal fun reset() {
        this.title = null
        this.items.clear()
        this.mapItems.clear()
        this.toBeAppend.clear()
        this.map = emptyList()
        this.playerInventoryClickHandler = { _, _ -> false }
    }

    internal fun renderOnce(
        menu: Menu,
        inventory: Inventory
    ): CompleteDynamicMenuPageSnapshot {
        this.reset()
        this.builder()
        val viewContext = CompleteDynamicMenuViewContext(menu)
        val finalItems =
            mutableMapOf<Int, MenuItemBuilder<CompleteDynamicMenuClickContext>.(CompleteDynamicMenuViewContext) -> Unit>()
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

        val realTitle = if (this.title != null) {
            component {
                title!!(viewContext)
            }
        } else {
            null
        }
        if (realTitle != null) {
            menu.updateTitle(
                realTitle
            )
        }
        val itemsSnapshot = mutableMapOf<Int, MenuItemSnapshot<CompleteDynamicMenuClickContext>>()
        for ((index, builder) in finalItems) {
            val menuItemBuilder = MenuItemBuilder<CompleteDynamicMenuClickContext>().apply { builder(viewContext) }
            val menuItem = MenuItemSnapshot(
                itemStack {
                    clone { menuItemBuilder.icon }
                    if (menuItemBuilder.icon == null || menuItemBuilder.icon!!.isEmpty || menuItemBuilder.icon!!.type.isAir || !menuItemBuilder.icon!!.type.isItem) {
                        return@itemStack
                    }
                    if (!menuItemBuilder.useItemInfoAsHover) {
                        DataComponentTypes.CUSTOM_NAME eq (menuItemBuilder.name ?: Component.text(" "))
                        if (menuItemBuilder.description != null) {
                            DataComponentTypes.LORE eq menuItemBuilder.description!!
                        }
                    }
                },
                menuItemBuilder.clickEvent
            )
            itemsSnapshot[index] = menuItem
            inventory.setItem(index, menuItem.icon)
        }

        return CompleteDynamicMenuPageSnapshot(
            realTitle,
            itemsSnapshot.toMap(),
            this.playerInventoryClickHandler
        )
    }

    override fun build(menu: Menu): CompleteDynamicMenuPage {
        return CompleteDynamicMenuPage(
            this,
            this.launchEffect,
            this.disposeEffect
        )
    }

}

fun completeDynamicMenuPage(menu: Menu, builder: CompleteDynamicMenuPageBuilder.() -> Unit): CompleteDynamicMenuPage {
    return CompleteDynamicMenuPageBuilder().apply { this.builder = builder }.build(menu)
}