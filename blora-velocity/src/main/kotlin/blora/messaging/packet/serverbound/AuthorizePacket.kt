package blora.messaging.packet.serverbound

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import io.netty.buffer.ByteBuf

class AuthorizePacket : Packet {

    var serverName: String = ""
    var password: String = ""

    override val type: PacketType
        get() = PacketType.AUTHORIZE

    override fun encode(byteBuf: ByteBuf) {
        val serverNameBytes = serverName.encodeToByteArray()
        val passwordBytes = password.encodeToByteArray()
        byteBuf.writeInt(serverNameBytes.size)
        byteBuf.writeBytes(serverNameBytes)
        byteBuf.writeInt(passwordBytes.size)
        byteBuf.writeBytes(passwordBytes)
    }

    override fun decode(byteBuf: ByteBuf) {
        val serverNameLength = byteBuf.readInt()
        val serverNameBytes = byteBuf.readBytes(serverNameLength)
        val passwordLength = byteBuf.readInt()
        val passwordBytes = byteBuf.readBytes(passwordLength)

        this.serverName = serverNameBytes.toString(Charsets.UTF_8)
        this.password = passwordBytes.toString(Charsets.UTF_8)
    }

}