package blora.messaging.packet.clientbound

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

object AuthorizedPacket : Packet {

    override val type: PacketType
        get() = PacketType.AUTHORIZED

    override fun encode(byteBuf: ByteBuf) {
    }

    override fun decode(byteBuf: ByteBuf) {
    }

}