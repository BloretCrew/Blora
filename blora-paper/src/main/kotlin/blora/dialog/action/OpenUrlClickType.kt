package blora.dialog.action

import kotlinx.serialization.Serializable

@Serializable
data class OpenUrlClickType(
    val url: String,
    val type: String = "open_url"
) : ClickType()