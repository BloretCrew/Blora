package blora.menu.v2.page.builder

import blora.extension.localization
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.context.MenuClickContext
import blora.menu.v2.context.MenuViewContext
import blora.menu.v2.item.MenuItemBuilder
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.util.randomString
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.text.ComponentKt

abstract class MenuPageBuilder<C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> {

    internal var pageId: String = randomString(16)
    internal var title: (ComponentKt.(V) -> Unit)? = null
    internal val items: MutableMap<Int, MenuItemBuilder<out C>.(V) -> Unit> = mutableMapOf()
    internal val mapItems: MutableMap<Char, MenuItemBuilder<out C>.(V) -> Unit> = mutableMapOf()
    internal val toBeAppend = mutableListOf<MenuItemBuilder<out C>.(V) -> Unit>()
    internal var map: List<String> = listOf()
    internal var playerInventoryClickHandler: (ItemStack, C) -> Boolean = { _, _ -> false }
    internal var launchEffect: (V) -> Unit = {}
    internal var disposeEffect: (V) -> Unit = {}

    infix fun Pair<Int, Int>.eq(builder: MenuItemBuilder<out C>.(V) -> Unit) {
        items[(this.second - 1) + ((this.first - 1) * 9)] = builder
    }

    infix fun Char.eq(builder: MenuItemBuilder<out C>.(V) -> Unit) {
        if (this == ' ')
            throw IllegalArgumentException("map key cannot be white space")
        mapItems[this] = builder
    }

    abstract fun build(menu: Menu): P

}

fun <C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> MenuPageBuilder<out C, out V, out P>.backButton() {
    1 to 1 eq { viewContext ->
        icon { material { Material.ARROW } }
        name {
            localization(viewContext.viewer) {
                this.menuButtonBack
            }
        }
        clickEvent {
            it.stack.pop()
        }
    }
}

fun <C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> MenuPageBuilder<out C, out V, out P>.pageId(provider: () -> String) {
    this.pageId = provider.invoke()
}

fun <C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> MenuPageBuilder<out C, out V, out P>.title(
    title: (ComponentKt.(V) -> Unit)
) {
    this.title = title
}

fun <C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> MenuPageBuilder<out C, out V, out P>.mapping(
    vararg lines: String
) {
    this.map = lines.toList()
}

fun <C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> MenuPageBuilder<out C, out V, out P>.inventoryClick(
    handler: (ItemStack, C) -> Boolean
) {
    this.playerInventoryClickHandler = handler
}

fun <C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> MenuPageBuilder<out C, out V, out P>.appendLastEmpty(
    builder: MenuItemBuilder<out C>.(V) -> Unit
) {
    this.toBeAppend.add(builder)
}

fun <C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> MenuPageBuilder<out C, out V, out P>.launchEffect(
    effect: (V) -> Unit
) {
    this.launchEffect = effect
}

fun <C : MenuClickContext, V : MenuViewContext, P : MenuPage<out C, out V>> MenuPageBuilder<out C, out V, out P>.disposeEffect(
    effect: (V) -> Unit
) {
    this.disposeEffect = effect
}