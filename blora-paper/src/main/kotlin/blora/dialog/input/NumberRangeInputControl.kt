@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.input

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
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
) : InputControl()