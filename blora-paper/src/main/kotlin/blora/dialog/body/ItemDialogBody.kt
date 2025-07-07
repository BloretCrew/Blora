@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.body

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

@Serializable
data class ItemDescription(
    @Contextual
    val contents: Component,
    val width: Int = 200
)

@Serializable
data class ItemStack(
    @Contextual
    val id: Key,
    val count: Int? = null,
    // TODO: components
)

@Serializable
data class ItemDialogBody(
    val item: ItemStack,
    val description: ItemDescription? = null,
    @SerialName("show_decorations")
    val showDecorations: Boolean = true,
    @SerialName("show_tooltip")
    val showTooltip: Boolean = true,
    val width: Int = 16,
    val height: Int = 16,
    val type: String = "minecraft:item"

) : DialogBody()