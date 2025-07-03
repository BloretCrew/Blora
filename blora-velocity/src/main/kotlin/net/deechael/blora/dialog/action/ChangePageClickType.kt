package net.deechael.blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtString

@Serializable
data class ChangePageClickType(
    val page: Int,
    val type: String = "change_page"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "page" to NbtInt(this.page),
                "type" to NbtString(this.type)
            )
        )
    }

}
