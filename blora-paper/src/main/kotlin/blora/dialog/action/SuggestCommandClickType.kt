package blora.dialog.action

import kotlinx.serialization.Serializable

@Serializable
data class SuggestCommandClickType(
    val command: String,
    val type: String = "suggest_command"
) : ClickType()