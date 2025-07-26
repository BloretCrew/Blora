package blora.guild

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

@Serializable
data class GuildEnderChest(
    val items: Map<Int, @Contextual ItemStack> = mapOf()
)
