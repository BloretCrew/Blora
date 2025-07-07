package blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtTag

@Serializable
data class DynamicCustomClickType(
    val id: String,
    val additions: NbtTag? = null,
    val type: String = "dynamic/custom"
) : ClickType()
