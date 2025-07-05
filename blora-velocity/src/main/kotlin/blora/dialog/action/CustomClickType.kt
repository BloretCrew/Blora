package blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString

@Serializable
data class CustomClickType(
    val id: String,
    val payload: String? = null,
    val type: String = "custom"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "id" to NbtString(this.id),
                "type" to NbtString(this.type)
            ).let { map ->
                if (this.payload != null) {
                    map.toMutableMap().apply {
                        this["payload"] = NbtString(this@CustomClickType.payload)
                    }
                } else {
                    map
                }
            }
        )
    }

}

