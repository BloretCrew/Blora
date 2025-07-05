package blora.messaging.packet.common

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

class PongPacket : Packet {

    var pingTime: Long = 0L
    var pongTime: Long = System.currentTimeMillis()

    override val type: PacketType
        get() = PacketType.PONG

    override fun encode(byteBuf: ByteBuf) {
        byteBuf.writeLong(pingTime)
        byteBuf.writeLong(pongTime)
    }

    override fun decode(byteBuf: ByteBuf) {
        this.pingTime = byteBuf.readLong()
        this.pongTime = byteBuf.readLong()
    }

}