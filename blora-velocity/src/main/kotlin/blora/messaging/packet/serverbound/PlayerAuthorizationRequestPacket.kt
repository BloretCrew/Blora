package blora.messaging.packet.serverbound

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

class PlayerAuthorizationRequestPacket : Packet {

    var playerName: String = ""

    override val type: PacketType
        get() = PacketType.PLAYER_AUTHORIZATION_REQUEST

    override fun encode(byteBuf: ByteBuf) {
        val bytes = this.playerName.encodeToByteArray()
        byteBuf.writeInt(bytes.size)
        byteBuf.writeBytes(bytes)
    }

    override fun decode(byteBuf: ByteBuf) {
        val length = byteBuf.readInt()
        val bytes = byteBuf.readBytes(length)
        this.playerName = bytes.toString(Charsets.UTF_8)
    }

}