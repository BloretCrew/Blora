@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.body

import blora.converter.toKnbt
import blora.extension.toNBT
import blora.serialization.nbt.compound
import kotlinx.serialization.*
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtTag
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Serializable
data class ItemDescription(
    @Contextual
    val contents: Component,
    val width: Int = 200
) {

    fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf<String, NbtTag>(
                "contents" to this.contents.toKnbt()
            ).let { map ->
                if (this.width != 200) { // because 200 is default value, not set to reduce size if same as default
                    map.toMutableMap().apply {
                        this["width"] = NbtInt(this@ItemDescription.width)
                    }
                } else {
                    map
                }
            }
        )
    }

}


@Serializable
data class ItemDialogBody(
    @Transient
    val item: ItemStack = ItemStack(Material.AIR),
    val description: ItemDescription? = null,
    @SerialName("show_decorations")
    val showDecorations: Boolean = true,
    @SerialName("show_tooltip")
    val showTooltip: Boolean = true,
    val width: Int = 16,
    val height: Int = 16,
    val type: String = "minecraft:item"

) : DialogBody() {

    override fun toNBT(): NbtCompound {
        return compound {
            "item" eq item.toNBT()
            "type" eq type
            if (description != null) {
                "description" eq description.toNBT()
            }
            "show_decorations" eq showDecorations
            "show_tooltip" eq showTooltip
            "width" eq width
            "height" eq height
        }
    }

}

