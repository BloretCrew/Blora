@file:OptIn(ExperimentalSerializationApi::class)

package net.deechael.blora.dialog.action

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtTag
import net.deechael.blora.converter.toKnbt
import net.kyori.adventure.text.Component

@Serializable
data class ClickAction(
    @Contextual
    val label: Component,
    @Contextual
    val tooltip: Component? = null,
    val width: Int = 150, // [1, 1024]
    val action: ClickType? = null
) {

    fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf<String, NbtTag>(
                "label" to this.label.toKnbt()
            ).let { map ->
                val mutable = map.toMutableMap()
                if (this.tooltip != null) {
                    mutable["tooltip"] = this.tooltip.toKnbt()
                }
                if (this.width != 150) { // because 150 is default value, not set to reduce size if same as default
                    mutable["width"] = NbtInt(this.width)
                }
                if (action != null) {
                    mutable["action"] = action.toNBT()
                }
                mutable.toMap()
            }
        )
    }

}
