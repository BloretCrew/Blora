package blora.messaging

import blora.messaging.packet.Packet
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import java.net.SocketAddress
import java.util.*

class BloraConnection(
    val channel: Channel
) {

    val address: SocketAddress
        get() = this.channel.remoteAddress()

    fun send(packet: Packet) {
        val byteBuf = Unpooled.buffer()
        byteBuf.writeInt(packet.type.id)
        packet.encode(byteBuf)
        this.channel.writeAndFlush(byteBuf)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null || javaClass != other.javaClass)
            return false
        val that = other as BloraConnection
        return this.channel == that.channel
    }

    override fun hashCode(): Int {
        return Objects.hash(this.channel)
    }

}