package blora.command

import blora.player.QuickPlayerWrapper
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CommandInvokerWrapper(
    private val commandSender: CommandSender
) : blora.internal.api.command.CommandInvoker {

    override val isPlayer: Boolean
        get() = this.commandSender is Player
    override val isBlock: Boolean
        get() = this.commandSender is BlockCommandSender
    override val isConsole: Boolean
        get() = this.commandSender is ConsoleCommandSender
    override val asBukkit: CommandSender
        get() = commandSender

    override fun player(): blora.internal.api.player.BloraPlayer {
        return QuickPlayerWrapper(this.commandSender as Player)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is blora.internal.api.command.CommandInvoker) {
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