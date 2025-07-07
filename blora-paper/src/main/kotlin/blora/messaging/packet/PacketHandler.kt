package blora.messaging.packet

import blora.messaging.BloraConnection

interface PacketHandler<T : Packet> {

    fun handlePacket(connection: BloraConnection, packet: T)

}