package blora.messaging.packet.clientbound

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

object ShutdownPacket : Packet {

    override val type: PacketType
        get() = PacketType.SHUTDOWN

    override fun encode(byteBuf: ByteBuf) {
    }

    override fun decode(byteBuf: ByteBuf) {
    }

}