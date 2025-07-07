package blora.dialog.action

import blora.dialog.Dialog
import kotlinx.serialization.Serializable

@Serializable
data class ShowDialogClickType(
    val dialog: String, // id or inline
    val type: String = "show_dialog"
) : ClickType()

@Serializable
data class ShowInlineDialogClickType(
    val dialog: Dialog,
    val type: String = "show_dialog"
) : ClickType()