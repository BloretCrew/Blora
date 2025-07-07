package blora.messaging.packet.serverbound

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

class PongPacket constructor() : Packet {

    constructor(pingTime: Long) : this() {
        this.pingTime = pingTime
        this.pongTime = System.currentTimeMillis()
    }

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