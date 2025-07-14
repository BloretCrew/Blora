package blora.dialog.action

import blora.listener.packet.CustomClickActionHandler
import blora.serialization.nbt.compound
import blora.util.randomString
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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

@Serializable
data class DynamicCustomClickTypeInjected(
    val additions: NbtTag? = null,
    val type: String = "dynamic/custom",
    @Transient
    val callback: (NbtTag?) -> Unit = {}
) : ClickType() {

    override fun toNBT(): NbtCompound {
        val identifier = randomString(32) + System.currentTimeMillis().toString()
        val finalTag = compound {
            "identifier" eq identifier
            if (additions != null) {
                "realTag" eq additions
            }
        }
        CustomClickActionHandler.resolving[identifier] = callback
        return compound {
            "id" eq "blora:custom_click"
            "type" eq type
            "additions" eq finalTag
        }
    }

}
