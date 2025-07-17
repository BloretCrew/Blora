package blora.command.argument

import blora.BloraPlugin
import blora.extension.sendPacket
import blora.protocol.play.clientbound.PlayerInfoRemovePacket
import blora.protocol.play.clientbound.PlayerInfoUpdatePacket
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.proxy.protocol.packet.brigadier.ArgumentPropertySerializer
import io.netty.buffer.ByteBuf

object SinglePlayerArgumentType : ArgumentType<Player> {

    fun getPlayer(context: CommandContext<*>, name: String): Player {
        return context.getArgument(name, Player::class.java)
    }

    fun removePlayer(player: Player) {
        BloraPlugin.proxyServer
            .allPlayers
            .filter { it != player }
            .forEach {
                it.sendPacket(PlayerInfoRemovePacket.create(player))
            }
    }

    fun notifyOtherPlayers(player: Player, specificCurrentServer: RegisteredServer? = null) {
        val currentServer = if (specificCurrentServer != null) {
            specificCurrentServer
        } else {
            val playerCurrent = player.currentServer
            if (playerCurrent.isEmpty) { // if player is not in any server, we don't know which players should be excluded
                return
            }
            playerCurrent.get().server
        }
        BloraPlugin.proxyServer
            .allPlayers
            .filter { it.currentServer.isPresent && it.currentServer.get().serverInfo.name != currentServer.serverInfo.name }
            .forEach { it.sendPacket(PlayerInfoUpdatePacket.createAddVirtualPlayer(player, false)) }
    }

    fun sendOtherServerPlayers(player: Player, specificCurrentServer: RegisteredServer? = null) {
        val currentServer = if (specificCurrentServer != null) {
            specificCurrentServer
        } else {
            val playerCurrent = player.currentServer
            if (playerCurrent.isEmpty) { // if player is not in any server, we don't know which players should be excluded
                return
            }
            playerCurrent.get().server
        }
        BloraPlugin.proxyServer
            .allServers
            .filter { it.serverInfo.name != currentServer.serverInfo.name }
            .map { it.playersConnected }
            .flatten()
            .forEach {
                player.sendPacket(PlayerInfoUpdatePacket.createAddVirtualPlayer(it, false))
            }
    }

    override fun parse(reader: StringReader): Player {
        val playerName = reader.readUnquotedString()
        val optionalPlayer = BloraPlugin.proxyServer.getPlayer(playerName)
        if (optionalPlayer.isEmpty)
            throw SimpleCommandExceptionType(LiteralMessage("该玩家不存在")).create()
        return optionalPlayer.get()
    }

}

object SinglePlayerArgumentPropertySerializer : ArgumentPropertySerializer<SinglePlayerArgumentType> {

    override fun deserialize(
        buf: ByteBuf,
        protocolVersion: ProtocolVersion
    ): SinglePlayerArgumentType {
        return SinglePlayerArgumentType
    }

    override fun serialize(
        `object`: SinglePlayerArgumentType,
        buf: ByteBuf,
        protocolVersion: ProtocolVersion
    ) {
        buf.writeByte(0x01 and 0x02)
    }

}