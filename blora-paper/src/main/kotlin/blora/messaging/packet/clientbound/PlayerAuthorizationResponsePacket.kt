package blora.messaging.packet.clientbound

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

class PlayerAuthorizationResponsePacket : Packet {

    var playerName: String = ""
    var authorized: Boolean = false

    override val type: PacketType
        get() = PacketType.PLAYER_AUTHORIZATION_RESPONSE

    override fun encode(byteBuf: ByteBuf) {
        val bytes = this.playerName.encodeToByteArray()
        byteBuf.writeInt(bytes.size)
        byteBuf.writeBytes(bytes)
        byteBuf.writeByte(if (authorized) 1 else 0)
    }

    override fun decode(byteBuf: ByteBuf) {
        val length = byteBuf.readInt()
        val bytes = byteBuf.readBytes(length)
        this.playerName = bytes.toString(Charsets.UTF_8)
        this.authorized = byteBuf.readByte() == 1.toByte()
    }

}