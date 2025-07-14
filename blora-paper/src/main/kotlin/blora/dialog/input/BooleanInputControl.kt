@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.input

import blora.converter.toKnbt
import blora.serialization.nbt.compound
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.kyori.adventure.text.Component

@Serializable
data class BooleanInputControl(
    val key: String,
    @Contextual
    val label: Component,
    val initial: Boolean = false,
    @SerialName("on_true")
    val onTrue: String = "true",
    @SerialName("on_false")
    val onFalse: String = "false"
) : InputControl() {

    override fun toNBT(): NbtCompound {
        return compound {
            "type" eq "minecraft:boolean"
            "key" eq key
            "label" eq label.toKnbt()
            if (initial) {
                "initial" eq true
            }
            if (onTrue != "true") {
                "onTrue" eq "true"
            }
            if (onTrue != "false") {
                "onFalse" eq "false"
            }
        }
    }

}

