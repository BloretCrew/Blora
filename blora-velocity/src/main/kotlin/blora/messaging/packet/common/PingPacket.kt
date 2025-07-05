package blora.messaging.packet.common

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

class PingPacket : Packet {

    var time: Long = System.currentTimeMillis()

    override val type: PacketType
        get() = PacketType.PING

    override fun encode(byteBuf: ByteBuf) {
        byteBuf.writeLong(time)
    }

    override fun decode(byteBuf: ByteBuf) {
        time = byteBuf.readLong()
    }

}