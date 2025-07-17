package blora.chat

import blora.extension.asDisplayName
import blora.menu.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object PlayerItemView {

    fun view(viewer: Player, item: ItemStack) {
        Menu(
            null,
            viewer,
            5,
            item.asDisplayName(),
            { it.destroy() }
        ) {
            lines(5)
            mapping(
                "#########",
                "#########",
                "#########",
                "#########",
                "#########"
            )
            '#' eq {
                icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
            }
            3 to 5 eq {
                icon(item)
                useItemInfoAsHover()
            }
        }.open()
    }

}