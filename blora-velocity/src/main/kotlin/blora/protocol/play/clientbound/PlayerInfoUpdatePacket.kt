package blora.protocol.play.clientbound

import blora.bytes.Byter
import blora.extension.writeEnumSet
import com.velocitypowered.api.proxy.Player
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.util.*

object PlayerInfoUpdatePacket {

    fun createAddVirtualPlayer(player: Player, listed: Boolean): ByteBuf {
        val byter = Byter()

        byter.writeByte(0x3F) // packet id

        byter.writeEnumSet(
            EnumSet.of(
                Action.ADD_PLAYER,
                Action.UPDATE_LISTED
            ),
            Action::class.java
        ) // actions

        byter.writeVarInt(1) // prefixed array length
        byter.writeUUID(player.uniqueId) // prefixed array [0] : uuid

        val usernameBytes = player.username.toByteArray(Charsets.UTF_8)
        byter.writeVarInt(usernameBytes.size) // prefixed array[0] : actions[0] : name
        byter.writeBytes(usernameBytes)
        byter.writeVarInt(0) // prefixed array[0] : actions[0] : property(prefixed array)

        byter.writeBoolean(listed)

        return Unpooled.copiedBuffer(byter.byteArray)
    }

    enum class Action {
        ADD_PLAYER,
        INITIALIZE_PLAYER,
        UPDATE_GAME_MODE,
        UPDATE_LISTED,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        UPDATE_LIST_ORDER,
        UPDATE_HAT
    }

}