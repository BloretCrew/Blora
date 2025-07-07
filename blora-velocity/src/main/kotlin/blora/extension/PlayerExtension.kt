package blora.extension

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import io.netty.buffer.ByteBuf
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt

fun Player.sendPacket(packet: ByteBuf) {
    (this as ConnectedPlayer).connection.channel.writeAndFlush(packet)
}

fun Player.disconnect(builder: ComponentKt.() -> Unit) {
    this.disconnect(component(builder))
}