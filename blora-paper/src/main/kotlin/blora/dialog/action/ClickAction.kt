@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.action

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

@Serializable
data class ClickAction(
    @Contextual
    val label: Component,
    @Contextual
    val tooltip: Component? = null,
    val width: Int = 150, // [1, 1024]
    val action: ClickType? = null
)