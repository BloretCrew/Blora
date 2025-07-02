package net.deechael.blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString

@Serializable
data class CopyToClipboardClickType(
    val value: String,
    val type: String = "copy_to_clipboard"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "value" to NbtString(this.value),
                "type" to NbtString(this.type)
            )
        )
    }

}
