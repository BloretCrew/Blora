@file:Suppress("UNCHECKED_CAST")

package blora.messaging.packet

import blora.messaging.BloraConnection

object PacketHandlerManager {

    private val handlers = mutableMapOf<PacketType, PacketHandler<Packet>>()
    private val authorizedHandlers = mutableMapOf<PacketType, PacketHandler<Packet>>()

    fun <T : Packet> register(authorized: Boolean, packetType: PacketType, handler: PacketHandler<T>) {
        val wrappedHandler = object : PacketHandler<Packet> {
            override fun handlePacket(connection: BloraConnection, packet: Packet) {
                handler.handlePacket(connection, packet as T)
            }
        }
        if (!authorized) {
            this.handlers[packetType] = wrappedHandler
        } else {
            this.authorizedHandlers[packetType] = wrappedHandler
        }
    }

    fun handle(connection: BloraConnection, packet: Packet) {
        val type = packet.type
        if (connection.isAuthorized) {
            val handler = this.authorizedHandlers[type]
            if (handler != null) {
                handler.handlePacket(connection, packet)
                return
            }
        }
        val handler = this.handlers[type]
        handler?.handlePacket(connection, packet)
    }

}