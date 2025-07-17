package blora.protocol.play.clientbound

import blora.bytes.Byter
import com.velocitypowered.api.proxy.Player
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

object PlayerInfoRemovePacket {

    fun create(player: Player): ByteBuf {
        val byter = Byter()

        byter.writeByte(0x3E) // packet id

        byter.writeVarInt(1) // prefixed array size
        byter.writeUUID(player.uniqueId) // prefixed array : [0]

        return Unpooled.copiedBuffer(byter.byteArray)
    }

}