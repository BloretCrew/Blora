package blora.messaging.packet

import blora.messaging.packet.clientbound.*
import blora.messaging.packet.common.DebugMessagePacket
import blora.messaging.packet.common.ReloadConfigurationPacket
import blora.messaging.packet.serverbound.AuthorizePacket
import blora.messaging.packet.serverbound.PlayerAuthorizationRequestPacket
import blora.messaging.packet.serverbound.PongPacket

enum class PacketType(
    val id: Int,
    val packetConstructor: () -> Packet
) {

    SHUTDOWN(0x00, { ShutdownPacket }),
    PING(0x01, { PingPacket() }),
    PONG(0x02, { PongPacket() }),
    UNAUTHORIZED(0x03, { UnauthorizedPacket }),
    AUTHORIZE(0x04, { AuthorizePacket() }),
    AUTHORIZATION_FAILED(0x05, { AuthorizationFailedPacket }),
    AUTHORIZED(0x06, { AuthorizedPacket }),
    DEBUG_MESSAGE(0x07, { DebugMessagePacket() }),
    PLAYER_AUTHORIZATION_REQUEST(0x08, { PlayerAuthorizationRequestPacket() }),
    PLAYER_AUTHORIZATION_RESPONSE(0x09, { PlayerAuthorizationResponsePacket() }),
    PLAYER_AUTHORIZATION_UPDATE(0x0a, { PlayerAuthorizationUpdatePacket() }),
    RELOAD_CONFIGURATION(0x0b, { ReloadConfigurationPacket })
    ;

    companion object {

        fun fromId(id: Int): PacketType? {
            return entries.firstOrNull { it.id == id }
        }

    }

}