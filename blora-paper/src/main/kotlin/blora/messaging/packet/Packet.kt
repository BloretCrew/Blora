package blora.messaging.packet

import io.netty.buffer.ByteBuf

interface Packet {

    val type: PacketType

    fun encode(byteBuf: ByteBuf)

    fun decode(byteBuf: ByteBuf)

}