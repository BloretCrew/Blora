@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog.action

import blora.converter.toKnbt
import blora.serialization.nbt.compound
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.kyori.adventure.text.Component

@Serializable
data class ClickAction(
    @Contextual
    val label: Component,
    @Contextual
    val tooltip: Component? = null,
    val width: Int = 150, // [1, 1024]
    val action: ClickType? = null
) {

    fun toNBT(): NbtCompound {
        return compound {
            "label" eq label.toKnbt()
            if (tooltip != null) {
                "tooltip" eq tooltip.toKnbt()
            }
            "width" eq width
            if (action != null) {
                "action" eq action.toNBT()
            }
        }
    }

}
