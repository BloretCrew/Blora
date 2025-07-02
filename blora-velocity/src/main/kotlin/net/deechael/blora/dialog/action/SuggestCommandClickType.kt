package net.deechael.blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString

@Serializable
data class SuggestCommandClickType(
    val command: String,
    val type: String = "suggest_command"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "command" to NbtString(this.command),
                "type" to NbtString(this.type)
            )
        )
    }

}
