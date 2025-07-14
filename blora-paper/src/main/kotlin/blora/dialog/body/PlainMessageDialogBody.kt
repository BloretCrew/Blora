package blora.dialog.body

import blora.converter.toKnbt
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtString
import net.kyori.adventure.text.Component

@Serializable
data class PlainMessageDialogBody(
    @Contextual
    val contents: Component,
    val width: Int = 200,
    val type: String = "minecraft:plain_message"
) : DialogBody() {

    override fun toNBT(): NbtCompound {
        return NbtCompound(
            mapOf(
                "contents" to this.contents.toKnbt(),
                "type" to NbtString(this.type)
            ).let { map ->
                if (this.width != 200) {
                    map.toMutableMap().apply {
                        this["width"] = NbtInt(this@PlainMessageDialogBody.width)
                    }
                } else {
                    map
                }
            }
        )
    }

}



