package blora.listener.packet

import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket

object CommandSuggestionHandler {

    fun handle(packet: ServerboundCommandSuggestionPacket): ServerboundCommandSuggestionPacket? {
        println("command suggestion: ${packet.command}")
        return packet
    }

}