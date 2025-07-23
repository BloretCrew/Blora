@file:Suppress("UnstableApiUsage")

package blora.menu.v2.item

import blora.item.itemStack
import blora.item.material
import blora.menu.v2.context.MenuClickContext
import blora.menu.v2.context.StaticMenuClickContext
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.space

class MenuItemSnapshot<C: MenuClickContext>(
    val icon: ItemStack?,
    val click: ((C) -> Unit)?
) {

    companion object {

        val PLACEHOLDER = itemStack {
            material { Material.BLACK_STAINED_GLASS_PANE }
            DataComponentTypes.CUSTOM_NAME eq component {
                space()
            }
        }!!

    }

}