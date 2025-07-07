@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.input

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
) : InputControl()