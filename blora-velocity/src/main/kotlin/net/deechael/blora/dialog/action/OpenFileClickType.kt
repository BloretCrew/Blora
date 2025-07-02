package net.deechael.blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString

@Serializable
data class OpenFileClickType(
    val path: String,
    val type: String = "open_file"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "path" to NbtString(this.path),
                "type" to NbtString(this.type)
            )
        )
    }

}
