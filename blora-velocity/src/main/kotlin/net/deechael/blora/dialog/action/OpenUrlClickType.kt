package net.deechael.blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString

@Serializable
data class OpenUrlClickType(
    val url: String,
    val type: String = "open_url"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "url" to NbtString(this.url),
                "type" to NbtString(this.type)
            )
        )
    }

}

