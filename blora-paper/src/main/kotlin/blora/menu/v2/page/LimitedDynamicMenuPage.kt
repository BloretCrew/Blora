@file:Suppress("UnstableApiUsage")

package blora.menu.v2.page

import blora.item.clone
import blora.item.itemStack
import blora.menu.v2.Menu
import blora.menu.v2.context.LimitedDynamicMenuClickContext
import blora.menu.v2.context.LimitedDynamicMenuViewContext
import blora.menu.v2.item.MenuItemBuilder
import blora.menu.v2.item.MenuItemSnapshot
import blora.menu.v2.page.snapshot.LimitedDynamicMenuPageSnapshot
import blora.menu.v2.page.snapshot.MenuPageSnapshot
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt

class LimitedDynamicMenuPage(
    override val pageId: String,
    val title: (ComponentKt.(LimitedDynamicMenuViewContext) -> Unit)? = null,
    val items: Map<Int, MenuItemBuilder<LimitedDynamicMenuClickContext>.(LimitedDynamicMenuViewContext) -> Unit>,
    val playerInventoryClickHandler: (ItemStack, LimitedDynamicMenuClickContext) -> Boolean,
    private val launchEffect: (LimitedDynamicMenuViewContext) -> Unit,
    private val disposeEffect: (LimitedDynamicMenuViewContext) -> Unit
) : MenuPage<LimitedDynamicMenuClickContext, LimitedDynamicMenuViewContext> {

    override fun fireLaunchEffect(menu: Menu) {
        this.launchEffect(LimitedDynamicMenuViewContext(menu))
    }

    override fun fireDisposeEffect(menu: Menu) {
        this.disposeEffect(LimitedDynamicMenuViewContext(menu))
    }

    override fun render(
        menu: Menu,
        inventory: Inventory
    ): MenuPageSnapshot<out LimitedDynamicMenuClickContext, out LimitedDynamicMenuViewContext> {
        val viewContext = LimitedDynamicMenuViewContext(menu)
        val realTitle = if (this.title != null) {
            component {
                title(viewContext)
            }
        } else {
            null
        }
        if (realTitle != null) {
            menu.updateTitle(
                realTitle
            )
        }
        val itemsSnapshot = mutableMapOf<Int, MenuItemSnapshot<LimitedDynamicMenuClickContext>>()
        for ((index, builder) in this.items) {
            val menuItemBuilder = MenuItemBuilder<LimitedDynamicMenuClickContext>().apply { builder(viewContext) }
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

        return LimitedDynamicMenuPageSnapshot(
            realTitle,
            itemsSnapshot.toMap(),
            this.playerInventoryClickHandler
        )
    }

}