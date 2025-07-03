@file:OptIn(ExperimentalSerializationApi::class)

package net.deechael.blora.dialog.input

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.deechael.blora.converter.toKnbt
import net.deechael.blora.serialization.nbt.compound
import net.kyori.adventure.text.Component

@Serializable
data class Multiline(
    @SerialName("max_lines")
    val maxLines: Int? = null,
    val height: Int? = null
) {

    fun toNBT(): NbtCompound? {
        if (this.maxLines == null && this.height == null) {
            return null
        }
        return compound {
            if (maxLines != null) {
                "max_lines" eq maxLines
            }
            if (height != null) {
                "height" eq height
            }
        }
    }

}

@Serializable
data class TextInputControl(
    val key: String,
    @Contextual
    val label: Component,
    val width: Int = 200,
    @SerialName("label_visible")
    val labelVisible: Boolean = true,
    val initial: String = "",
    @SerialName("max_length")
    val maxLength: Int = 32,
    val multiline: Multiline? = null,
) : InputControl() {

    override fun toNBT(): NbtCompound {
        return compound {
            "type" eq "minecraft:text"
            "key" eq key
            "label" eq label.toKnbt()
            if (width != 200) {
                "width" eq width
            }
            if (!labelVisible) {
                "label_visible" eq false
            }
            if (initial != "") {
                "initial" eq initial
            }
            if (maxLength != 32) {
                "max_length" eq maxLength
            }
            if (multiline != null) {
                val nbt = multiline.toNBT()
                if (nbt != null) {
                    "multiline" eq nbt
                }
            }
        }
    }

}