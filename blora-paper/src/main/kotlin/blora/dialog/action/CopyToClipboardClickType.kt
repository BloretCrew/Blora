package blora.dialog.action

import kotlinx.serialization.Serializable

@Serializable
data class CopyToClipboardClickType(
    val value: String,
    val type: String = "copy_to_clipboard"
) : ClickType()