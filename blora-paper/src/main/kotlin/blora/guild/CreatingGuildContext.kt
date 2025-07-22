package blora.guild

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class CreatingGuildContext(
    var id: String = "",
    var displayName: String = "",
    var icon: ItemStack = ItemStack(Material.STONE),
)
