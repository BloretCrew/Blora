package blora.api.player

import org.bukkit.entity.Player
import blora.api.command.CommandInvoker

interface QuickPlayer : CommandInvoker {

    override val asBukkit: Player

}