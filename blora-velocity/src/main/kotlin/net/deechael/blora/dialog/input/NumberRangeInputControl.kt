@file:OptIn(ExperimentalSerializationApi::class)

package net.deechael.blora.dialog.input

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.deechael.blora.converter.toKnbt
import net.deechael.blora.serialization.nbt.compound
import net.kyori.adventure.text.Component

@Serializable
data class NumberRangeInputControl(
    val key: String,
    @Contextual
    val label: Component,
    val start: Float, // inclusive
    val end: Float, // inclusive
    val step: Float? = null,
    val labelFormat: String = "options.generic_value",
    val width: Int = 200,
    val initial: Float? = null, // will be rounded down nearest step, must be within range
) : InputControl() {

    override fun toNBT(): NbtCompound {
        return compound {
            "type" eq "minecraft:number_range"
            "key" eq key
            "label" eq label.toKnbt()
            "start" eq start
            "end" eq end
            if (step != null) {
                "step" eq step
            }
            if (labelFormat == "options.generic_value") {
                "labelFormat" eq labelFormat
            }
            if (width != 200) {
                "width" eq width
            }
            if (initial != null) {
                "initial" eq initial
            }
        }
    }

}
