package blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString

@Serializable
data class RunCommandClickType(
    val command: String,
    val type: String = "run_command"
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

