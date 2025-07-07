@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.input

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

@Serializable
data class Multiline(
    @SerialName("max_lines")
    val maxLines: Int? = null,
    val height: Int? = null
)

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
) : InputControl()