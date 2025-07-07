package blora.dialog.action

import kotlinx.serialization.Serializable

@Serializable
data class DynamicRunCommandClickType(
    val template: String,
    val type: String = "dynamic/run_command"
) : ClickType()