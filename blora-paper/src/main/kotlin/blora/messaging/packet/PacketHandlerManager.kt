@file:Suppress("UNCHECKED_CAST")

package blora.messaging.packet

import blora.messaging.BloraConnection

object PacketHandlerManager {

    private val handlers = mutableMapOf<PacketType, PacketHandler<Packet>>()

    fun <T : Packet> register(packetType: PacketType, handler: PacketHandler<T>) {
        if (this.handlers.containsKey(packetType)) {
            throw IllegalStateException("PacketType $packetType already has a registered handler")
        }
        val wrappedHandler = object : PacketHandler<Packet> {
            override fun handlePacket(connection: BloraConnection, packet: Packet) {
                handler.handlePacket(connection, packet as T)
            }
        }
        this.handlers[packetType] = wrappedHandler
    }

    fun handle(connection: BloraConnection, packet: Packet) {
        val type = packet.type
        val handler = this.handlers[type]
        handler?.handlePacket(connection, packet)
    }

}