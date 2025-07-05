package blora.messaging.packet.clientbound

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

object AuthorizationFailedPacket : Packet {

    override val type: PacketType
        get() = PacketType.AUTHORIZATION_FAILED

    override fun encode(byteBuf: ByteBuf) {
    }

    override fun decode(byteBuf: ByteBuf) {
    }

}