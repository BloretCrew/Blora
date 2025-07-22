package blora.menu

import blora.adventure.itemLore
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt

class MenuContext(
    val menu: Menu,
    val clickType: ClickType,
    val inventoryAction: InventoryAction
) {

    val stack: MenuStack
        get() = this.menu.stack

}

class MenuItem(
    val icon: ItemStack? = null,
    val useItemInfoAsHover: Boolean = false,
    val clickEvent: ((menu: MenuPageContext) -> Unit)? = null,
    val hoverText: ((menu: Menu) -> HoverText) = { HoverText() },
)

class HoverText(
    val title: Component? = Component.text(" "),
    val description: ItemLore? = null
)

class MenuItemBuilder {

    internal var icon: ItemStack? = null
    internal var useItemInfoAsHover = false
    internal var clickEvent: ((menu: MenuPageContext) -> Unit)? = null
    internal var hoverText: ((menu: Menu) -> HoverText) = { HoverText() }

    fun build(): MenuItem {
        return MenuItem(
            icon = icon,
            useItemInfoAsHover = useItemInfoAsHover,
            clickEvent = clickEvent,
            hoverText = hoverText,
        )
    }

}

fun MenuItemBuilder.icon(item: ItemStack) {
    this.icon = item.clone() // secure in api to ensure the original item won't be modified
}

fun MenuItemBuilder.useItemInfoAsHover() {
    this.useItemInfoAsHover = true
}

fun MenuItemBuilder.clickEvent(block: (menu: MenuPageContext) -> Unit) {
    this.clickEvent = block
}

fun MenuItemBuilder.hoverText(block: HoverTextBuilder.(menu: Menu) -> Unit) {
    this.hoverText = { menu ->
        HoverTextBuilder().apply {
            this.block(menu)
        }.build()
    }
}

fun menuItem(builder: MenuItemBuilder.() -> Unit): MenuItem {
    return MenuItemBuilder().apply(builder).build()
}

class HoverTextBuilder {

    internal var title: Component? = null
    internal var description: ItemLore? = null

    fun build(): HoverText {
        return HoverText(title, description)
    }

}

fun HoverTextBuilder.title(builder: ComponentKt.() -> Unit) {
    this.title = component(builder)
}

fun HoverTextBuilder.description(builder: ComponentKt.() -> Unit) {
    this.description = itemLore(builder)
}