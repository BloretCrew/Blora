package blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.benwoodworth.knbt.NbtTag

@Serializable
data class DynamicCustomClickType(
    val id: String,
    val additions: NbtTag? = null,
    val type: String = "dynamic/custom"
) : ClickType() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf<String, NbtTag>(
                "id" to NbtString(this.id),
                "type" to NbtString(this.type)
            ).let { map ->
                if (this.additions != null) {
                    map.toMutableMap().apply {
                        this["additions"] = this@DynamicCustomClickType.additions
                    }
                } else {
                    map
                }
            }
        )
    }

}


