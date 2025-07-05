package blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString

@Serializable
data class DynamicRunCommandClickType(
    val template: String,
    val type: String = "dynamic/run_command"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "template" to NbtString(this.template),
                "type" to NbtString(this.type)
            )
        )
    }

}

