package blora.messaging.packet.common

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

object ReloadConfigurationPacket : Packet {

    override val type: PacketType
        get() = PacketType.RELOAD_CONFIGURATION

    override fun encode(byteBuf: ByteBuf) {
    }

    override fun decode(byteBuf: ByteBuf) {
    }

}