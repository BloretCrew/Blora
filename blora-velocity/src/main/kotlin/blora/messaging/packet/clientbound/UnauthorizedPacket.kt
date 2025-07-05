package blora.messaging.packet.clientbound

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

object UnauthorizedPacket : Packet {

    override val type: PacketType
        get() = PacketType.UNAUTHORIZED

    override fun encode(byteBuf: ByteBuf) {
    }

    override fun decode(byteBuf: ByteBuf) {
    }

}