@file:OptIn(ExperimentalSerializationApi::class)

package net.deechael.blora.dialog

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtList
import net.benwoodworth.knbt.buildNbtCompound
import net.deechael.blora.converter.toKnbt
import net.deechael.blora.dialog.action.ClickAction
import net.deechael.blora.dialog.body.DialogBody
import net.deechael.blora.dialog.input.InputControl
import net.deechael.blora.serialization.nbt.compound
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
        buildNbtCompound {  }
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
            if (!canCloseWithEscape) {
                "can_close_with_escape" eq false
            }
            if (!pause) {
                "pause" eq false
            }
            if (afterAction != AfterAction.CLOSE) {
                "after_action" eq afterAction.toNBT()
            }
            if (yes != defaultYes) {
                "yes" eq yes.toNBT()
            }
            if (no != defaultNo) {
                "no" eq no.toNBT()
            }

        }
    }

}
