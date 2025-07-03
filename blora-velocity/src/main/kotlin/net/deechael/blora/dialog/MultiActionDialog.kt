@file:OptIn(ExperimentalSerializationApi::class)

package net.deechael.blora.dialog

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtList
import net.deechael.blora.converter.toKnbt
import net.deechael.blora.dialog.action.ClickAction
import net.deechael.blora.dialog.body.DialogBody
import net.deechael.blora.dialog.input.InputControl
import net.deechael.blora.serialization.nbt.compound
import net.kyori.adventure.text.Component

@Serializable
data class MultiActionDialog(
    @Contextual
    val title: Component,
    val actions: List<ClickAction>, // not empty
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
    val columns: Int = 2,
    @SerialName("exit_action")
    val exitAction: ClickAction? = null
) : Dialog() {

    override fun toNBT(): NbtCompound {
        return compound {
            "type" eq "minecraft:multi_action"
            "title" eq title.toKnbt()
            "actions" eq NbtList(actions.map { it.toNBT() })
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
            if (exitAction != null) {
                "exit_action" eq exitAction.toNBT()
            }
            "columns" eq columns
        }
    }

}
