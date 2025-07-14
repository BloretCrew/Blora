@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog

import blora.converter.toKnbt
import blora.dialog.action.ClickAction
import blora.dialog.body.DialogBody
import blora.dialog.input.InputControl
import blora.serialization.nbt.compound
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtList
import net.kyori.adventure.text.Component

internal val defaultYes = ClickAction(
    label = Component.translatable("gui.ok")
)

internal val defaultNo = ClickAction(
    label = Component.translatable("gui.no")
)

@Serializable
data class ConfirmationDialog(
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
    val yes: ClickAction = defaultYes,
    val no: ClickAction = defaultNo
) : Dialog() {

    override fun toNBT(): NbtCompound {
        return compound {
            "type" eq "minecraft:confirmation"
            "title" eq title.toKnbt()
            if (externalTitle != null) {
                "external_title" eq externalTitle.toKnbt()
            }
            if (body != null && body.isNotEmpty()) {
                "body" eq NbtList(body.map { it.toNBT() })
            }
            if (inputs != null && inputs.isNotEmpty()) {
                "inputs" eq NbtList(inputs.map { it.toNBT() })
            }
            "can_close_with_escape" eq canCloseWithEscape
            "pause" eq pause
            "after_action" eq afterAction.toNBT()
            "yes" eq yes.toNBT()
            "no" eq no.toNBT()
        }
    }

}
