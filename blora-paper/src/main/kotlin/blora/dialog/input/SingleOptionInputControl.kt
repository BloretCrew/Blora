@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.input

import blora.converter.toKnbt
import blora.serialization.nbt.compound
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtList
import net.kyori.adventure.text.Component

@Serializable
data class InputControlOption(
    val id: String,
    @Contextual
    val display: Component? = null,
    val initial: Boolean = false
) {

    fun toNBT(): NbtCompound {
        return compound {
            "id" eq id
            if (display != null) {
                "display" eq display.toKnbt()
            }
            if (initial) {
                "initial" eq true
            }
        }
    }

}

@Serializable
data class SingleOptionInputControl(
    val key: String,
    @Contextual
    val label: Component,
    val options: List<InputControlOption>,
    @SerialName("label_visible")
    val labelVisible: Boolean = true,
    val width: Int = 200
) : InputControl() {

    override fun toNBT(): NbtCompound {
        return compound {
            "type" eq "minecraft:single_option"
            "key" eq key
            "label" eq label.toKnbt()
            if (options.isNotEmpty()) {
                "options" eq NbtList(
                    options.map { it.toNBT() }
                )
            }
            if (!labelVisible) {
                "label_visible" eq false
            }
            if (width != 200) {
                "width" eq width
            }
        }
    }

}
