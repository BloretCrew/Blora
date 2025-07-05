package blora.extension

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import io.netty.buffer.ByteBuf

fun Player.sendPacket(packet: ByteBuf) {
    (this as ConnectedPlayer).connection.channel.writeAndFlush(packet)
}