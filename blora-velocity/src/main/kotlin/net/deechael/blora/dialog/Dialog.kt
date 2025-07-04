package net.deechael.blora.dialog

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.deechael.blora.extension.appendNbtString
import net.deechael.blora.extension.writeVarInt
import net.deechael.blora.serialization.nbt.writeToByteBufRoot

@Serializable
sealed class Dialog /*: DialogLike*/ {

    abstract fun toNBT(): NbtCompound

}

enum class DialogPacketState(val id: Int) {

    PLAY(0x85), CONFIGURATION(0x12)

}

fun Dialog.toJson(): String {
    return this.toNBT().entries.joinToString(separator = ",", prefix = "{", postfix = "}") { (name, value) ->
        buildString {
            appendNbtString(name, true)
            append(':')
            append(value)
        }
    }
}

fun Dialog.asPacket(state: DialogPacketState = DialogPacketState.PLAY): ByteBuf {
    val byteBuf = Unpooled.buffer()
    byteBuf.writeVarInt(state.id)
    if (state == DialogPacketState.PLAY) {
        byteBuf.writeVarInt(0)
    }
    this.toNBT().writeToByteBufRoot(byteBuf)
    return byteBuf
}