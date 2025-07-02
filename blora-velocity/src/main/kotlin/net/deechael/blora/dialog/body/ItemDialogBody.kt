@file:OptIn(ExperimentalSerializationApi::class)

package net.deechael.blora.dialog.body

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.*
import net.deechael.blora.converter.toKnbt
import net.deechael.blora.item.ItemStack
import net.kyori.adventure.text.Component

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
    val item: ItemStack,
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
        return NbtCompound(
            mapOf<String, NbtTag>(
                "item" to this.item.toNBT(),
                "type" to NbtString(this.type)
            ).let { map ->
                val mutable = map.toMutableMap()

                if (this.description != null) {
                    mutable["description"] = this.description.toNBT()
                }
                if (!this.showDecorations) { // only set when is not default value
                    mutable["show_decorations"] = NbtByte(false)
                }
                if (!this.showTooltip) { // only set when is not default value
                    mutable["show_tooltip"] = NbtByte(false)
                }
                if (this.width != 16) {
                    mutable["width"] = NbtInt(this.width)
                }
                if (this.height != 16) {
                    mutable["height"] = NbtInt(this.height)
                }

                mutable.toMap()
            }
        )
    }

}

