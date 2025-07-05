package blora.messaging.packet.common

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

class DebugMessagePacket : Packet {

    var message: String = ""

    override val type: PacketType
        get() = PacketType.DEBUG_MESSAGE

    override fun encode(byteBuf: ByteBuf) {
        val bytes = message.encodeToByteArray()
        byteBuf.writeInt(bytes.size)
        byteBuf.writeBytes(bytes)
    }

    override fun decode(byteBuf: ByteBuf) {
        val length = byteBuf.readInt()
        val bytes = byteBuf.readBytes(length)
        this.message = bytes.toString(Charsets.UTF_8)
    }

}