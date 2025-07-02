package net.deechael.blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.deechael.blora.dialog.Dialog

@Serializable
data class ShowDialogClickType(
    val dialog: String, // id or inline
    val type: String = "show_dialog"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "dialog" to NbtString(this.dialog),
                "type" to NbtString(this.type)
            )
        )
    }

}


@Serializable
data class ShowInlineDialogClickType(
    val dialog: Dialog,
    val type: String = "show_dialog"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "dialog" to this.dialog.toNBT(),
                "type" to NbtString(this.type)
            )
        )
    }

}
