package blora.dialog.action

import kotlinx.serialization.Serializable

@Serializable
data class CustomClickType(
    val id: String,
    val payload: String? = null,
    val type: String = "custom"
) : ClickType()