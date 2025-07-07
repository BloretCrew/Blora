package blora.dialog.action

import kotlinx.serialization.Serializable

@Serializable
data class OpenFileClickType(
    val path: String,
    val type: String = "open_file"
) : ClickType()