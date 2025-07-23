@file:Suppress("UnstableApiUsage")

package blora.menu.v2.item

import blora.adventure.itemLore
import blora.item.ItemBuilder
import blora.menu.v2.context.MenuClickContext
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt

class MenuItemBuilder<C : MenuClickContext> {

    internal var icon: ItemStack? = null
    internal var useItemInfoAsHover = false
    internal var name: Component? = Component.text(" ")
    internal var description: ItemLore? = null
    internal var clickEvent: ((context: C) -> Unit)? = null

}

fun <C : MenuClickContext> MenuItemBuilder<out C>.icon(builder: ItemBuilder.() -> Unit) {
    this.icon = ItemBuilder().apply(builder).build()
}

fun <C : MenuClickContext> MenuItemBuilder<out C>.useItemInfoAsHover() {
    this.useItemInfoAsHover = true
}

fun <C : MenuClickContext> MenuItemBuilder<out C>.name(builder: ComponentKt.() -> Unit) {
    this.name = component(builder)
}

fun <C : MenuClickContext> MenuItemBuilder<out C>.description(builder: ComponentKt.() -> Unit) {
    this.description = itemLore(builder)
}


fun <C : MenuClickContext> MenuItemBuilder<out C>.clickEvent(handler: (context: C) -> Unit) {
    this.clickEvent = handler
}