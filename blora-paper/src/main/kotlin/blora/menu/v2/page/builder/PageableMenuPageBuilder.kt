@file:Suppress("UnstableApiUsage")

package blora.menu.v2.page.builder

import blora.extension.localization
import blora.item.clone
import blora.item.itemStackOrNull
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.context.PageableMenuClickContext
import blora.menu.v2.context.PageableMenuViewContext
import blora.menu.v2.item.*
import blora.menu.v2.page.PageableDataProvider
import blora.menu.v2.page.PageableMenuPage
import blora.menu.v2.page.snapshot.PageableDynamicMenuPageSnapshot
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.space
import kotlin.math.min

class PageableMenuPageBuilder<D>(
    internal val dataProvider: PageableDataProvider<D>,
) : MenuPageBuilder<PageableMenuClickContext<D>, PageableMenuViewContext<D>, PageableMenuPage<D>>() {

    internal var showBackButton = false
    internal var dataItemBuilder: MenuItemBuilder<PageableMenuClickContext<D>>.(PageableMenuViewContext<D>, D, Int) -> Unit =
        { _, _, _ -> }
    internal var builder: PageableMenuPageBuilder<D>.() -> Unit = {}

    private var previousRecordedPageId: String? = null

    internal fun reset() {
        this.previousRecordedPageId = this.pageId
        this.title = null
        this.items.clear()
        this.mapItems.clear()
        this.toBeAppend.clear()
        this.map = emptyList()
        this.playerInventoryClickHandler = { _, _ -> false }
    }

    internal fun renderOnce(
        menuPage: PageableMenuPage<D>,
        page: Int,
        menu: Menu,
        inventory: Inventory
    ): PageableDynamicMenuPageSnapshot<D> {
        this.reset()
        if (menu.lines <= 2) {
            val finalItems = buildMap<Int, MenuItemSnapshot<PageableMenuClickContext<D>>> {
                for (index in 0 until menu.lines * 9) {
                    this[index] = MenuItemSnapshot(
                        MenuItemSnapshot.PLACEHOLDER
                    ) {}
                }
            }.toMutableMap()
            if (this.showBackButton) {
                finalItems[0] = MenuItemSnapshot(
                    itemStackOrNull {
                        material { Material.ARROW }
                        DataComponentTypes.CUSTOM_NAME eq component {
                            localization(menu.viewer) {
                                this.menuButtonBack
                            }
                        }
                    }
                ) {
                    it.stack.pop()
                }
            }
            return PageableDynamicMenuPageSnapshot(
                menuPage,
                1,
                1,
                null,
                emptyMap(),
                { _, _ -> false },
            )
        }
        this.builder()
        if (this.previousRecordedPageId != null && this.pageId != previousRecordedPageId) {
            throw RuntimeException("page id shouldn't be dynamic!")
        }
        this.dataProvider.refresh()
        var currentPage = page
        val maxPage = this.calculateMaxPage(menu.lines)
        while (currentPage > maxPage && currentPage > 1) {
            currentPage--
        }
        menuPage.currentPage = currentPage
        val viewContext = PageableMenuViewContext<D>(menu, currentPage, maxPage, this.dataProvider)
        val finalItems =
            mutableMapOf<Int, MenuItemBuilder<PageableMenuClickContext<D>>.(PageableMenuViewContext<D>) -> Unit>()
        for (i in 0..8) {
            finalItems[i] = {
                icon {
                    material { Material.BLACK_STAINED_GLASS_PANE }
                    DataComponentTypes.CUSTOM_NAME eq component {
                        space()
                    }
                }
                useItemInfoAsHover()
            }
        }
        for (i in ((menu.lines - 1) * 9)..((menu.lines * 9) - 1)) {
            finalItems[i] = {
                icon {
                    material { Material.BLACK_STAINED_GLASS_PANE }
                    DataComponentTypes.CUSTOM_NAME eq component {
                        space()
                    }
                }
                useItemInfoAsHover()
            }
        }
        for (i in 1 until menu.lines - 1) {
            finalItems[i * 9] = {
                icon {
                    material { Material.BLACK_STAINED_GLASS_PANE }
                    DataComponentTypes.CUSTOM_NAME eq component {
                        space()
                    }
                }
                useItemInfoAsHover()
            }
            finalItems[(i + 1) * 9 - 1] = {
                icon {
                    material { Material.BLACK_STAINED_GLASS_PANE }
                    DataComponentTypes.CUSTOM_NAME eq component {
                        space()
                    }
                }
                useItemInfoAsHover()
            }
        }
        if (this.showBackButton) {
            finalItems[0] = {
                icon {
                    material { Material.ARROW }
                }
                name {
                    localization(menu.viewer) {
                        this.menuButtonBack
                    }
                }
                clickEvent {
                    it.stack.pop()
                }
            }
        }
        if (currentPage > 1) {
            finalItems[(menu.lines - 1) * 9] = {
                icon {
                    material { Material.ARROW }
                }
                name {
                    localization(menu.viewer) {
                        this.menuButtonPrevious_page
                    }
                }
                clickEvent {
                    menuPage.currentPage -= 1
                    it.menu.rerender()
                }
            }
        }
        if (currentPage < maxPage) {
            finalItems[menu.lines * 9 - 1] = {
                icon {
                    material { Material.ARROW }
                }
                name {
                    localization(menu.viewer) {
                        this.menuButtonPrevious_page
                    }
                }
                clickEvent {
                    menuPage.currentPage += 1
                    it.menu.rerender()
                }
            }
        }
        for (rowIndex in (0 until min(menu.lines, this.map.size))) {
            val row = this.map[rowIndex]
            for (columnIndex in (0 until min(9, row.length))) {
                val realIndex = rowIndex * 9 + columnIndex
                if (isIgnored(menu.lines, realIndex, this.showBackButton))
                    continue
                val column = row[columnIndex]
                if (column == ' ')
                    continue
                val menuItem = mapItems[column]
                if (menuItem == null)
                    continue
                finalItems[realIndex] = menuItem
            }
        }
        for ((index, item) in this.items) {
            if (isIgnored(menu.lines, index, this.showBackButton))
                continue
            finalItems[index] = item
        }

        for (index in ((currentPage - 1) * ((menu.lines - 2) * 7)) until min(
            (currentPage * ((menu.lines - 2) * 7)),
            this.dataProvider.size
        )) {
            val counterIndex = index - (currentPage - 1) * ((menu.lines - 2) * 7)
            val pair = ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2)
            val realIndex = (pair.second - 1) + ((pair.first - 1) * 9)
            finalItems[realIndex] = { dataItemBuilder(it, it.dataProvider[index], index) }
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

        val itemsSnapshot = mutableMapOf<Int, MenuItemSnapshot<PageableMenuClickContext<D>>>()
        for ((index, builder) in finalItems) {
            val menuItemBuilder = MenuItemBuilder<PageableMenuClickContext<D>>().apply { builder(viewContext) }
            val menuItem = MenuItemSnapshot(
                itemStackOrNull {
                    clone { menuItemBuilder.icon }
                    if (menuItemBuilder.icon == null || menuItemBuilder.icon!!.isEmpty || menuItemBuilder.icon!!.type.isAir || !menuItemBuilder.icon!!.type.isItem) {
                        return@itemStackOrNull
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

        return PageableDynamicMenuPageSnapshot(
            menuPage,
            currentPage,
            maxPage,
            realTitle,
            itemsSnapshot,
            this.playerInventoryClickHandler
        )
    }

    internal fun calculateMaxPage(lines: Int): Int {
        if (lines <= 2) {
            return 1
        }
        val actualLines = lines - 2
        return (this.dataProvider.size / (7 * actualLines)) + (if (this.dataProvider.size % (7 * actualLines) == 0) 0 else 1)
    }

    override fun build(menu: Menu): PageableMenuPage<D> {
        this.builder()
        this.reset()
        // generate page id
        return PageableMenuPage(
            this.pageId,
            this,
            this.launchEffect,
            this.disposeEffect
        )
    }

    companion object {

        fun isIgnored(lines: Int, index: Int, showBackButton: Boolean): Boolean {
            if (lines <= 2) {
                return true
            }
            if (index == 0 && showBackButton) {
                return true
            }
            for (i in 1 until lines - 1) {
                return index in (i * 9 + 1)..((i + 1) * 9 - 2)
            }
            return index == (lines * 9 - 1) || index == ((lines - 1) * 9)
        }

    }

}

fun <D> pageableMenuPage(
    menu: Menu,
    dataProvider: PageableDataProvider<D>,
    builder: PageableMenuPageBuilder<D>.() -> Unit
): PageableMenuPage<D> {
    return PageableMenuPageBuilder(dataProvider).apply {
        this.builder = builder
    }.build(menu)
}

fun <D> PageableMenuPageBuilder<D>.showBackButton() {
    this.showBackButton = true
}

fun <D> PageableMenuPageBuilder<D>.dataItem(builder: MenuItemBuilder<PageableMenuClickContext<D>>.(viewContext: PageableMenuViewContext<D>, data: D, index: Int) -> Unit) {
    this.dataItemBuilder = builder
}