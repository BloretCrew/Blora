@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog

import blora.dialog.action.ClickAction
import blora.dialog.body.DialogBody
import blora.dialog.input.InputControl
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

@Serializable
data class ServerLinksDialog(
    @Contextual
    val title: Component,
    @SerialName("external_title")
    @Contextual
    val externalTitle: Component? = null,
    val body: List<DialogBody>? = null,
    val inputs: List<InputControl>? = null,
    @SerialName("can_close_with_escape")
    val canCloseWithEscape: Boolean = true,
    val pause: Boolean = true,
    @SerialName("after_action")
    val afterAction: AfterAction = AfterAction.CLOSE,
    @Contextual
    @SerialName("exit_action")
    val exitAction: ClickAction? = null,
    val columns: Int = 2,
    @SerialName("button_width")
    val buttonWidth: Int = 150
) : Dialog() {

    override fun toNms(): net.minecraft.server.dialog.Dialog {
        TODO("Not yet implemented")
    }

}