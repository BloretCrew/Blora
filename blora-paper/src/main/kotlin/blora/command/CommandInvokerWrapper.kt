package blora.command

import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import blora.player.QuickPlayerWrapper

open class CommandInvokerWrapper(
    private val commandSender: CommandSender
) : blora.api.command.CommandInvoker {

    override val isPlayer: Boolean
        get() = this.commandSender is Player
    override val isBlock: Boolean
        get() = this.commandSender is BlockCommandSender
    override val isConsole: Boolean
        get() = this.commandSender is ConsoleCommandSender
    override val asBukkit: CommandSender
        get() = commandSender

    override fun asPlayer(): blora.api.player.QuickPlayer {
        return QuickPlayerWrapper(this.commandSender as Player)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is blora.api.command.CommandInvoker) {
            return false
        }
        if (this === other) {
            return true
        }
        return this.commandSender == other.asBukkit
    }

    override fun hashCode(): Int {
        return commandSender.hashCode() + 100000000
    }

}