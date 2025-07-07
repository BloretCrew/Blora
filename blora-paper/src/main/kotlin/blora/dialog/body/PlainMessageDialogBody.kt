package blora.dialog.body

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

@Serializable
data class PlainMessageDialogBody(
    @Contextual
    val contents: Component,
    val width: Int = 200,
    val type: String = "minecraft:plain_message"
) : DialogBody()


