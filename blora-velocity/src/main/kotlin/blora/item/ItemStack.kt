package blora.item

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtString
import net.benwoodworth.knbt.NbtTag
import net.kyori.adventure.key.Key

@Serializable
data class ItemStack(
    @Contextual
    val id: Key,
    val count: Int? = null,
    // TODO: components
) {

    fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf<String, NbtTag>(
                "id" to NbtString(this.id.asString())
            ).let { map ->
                if (this.count != null) {
                    map.toMutableMap().apply {
                        this["count"] = NbtInt(this@ItemStack.count)
                    }
                } else {
                    map
                }
            }
        )
    }

}