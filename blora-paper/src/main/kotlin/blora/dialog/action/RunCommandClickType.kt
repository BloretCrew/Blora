package blora.dialog.action

import kotlinx.serialization.Serializable

@Serializable
data class RunCommandClickType(
    val command: String,
    val type: String = "run_command"
) : ClickType()