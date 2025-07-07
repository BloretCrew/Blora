@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.input

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

@Serializable
data class InputControlOption(
    val id: String,
    @Contextual
    val display: Component? = null,
    val initial: Boolean = false
)

@Serializable
data class SingleOptionInputControl(
    val key: String,
    @Contextual
    val label: Component,
    val options: List<InputControlOption>,
    @SerialName("label_visible")
    val labelVisible: Boolean = true,
    val width: Int = 200
) : InputControl()