package blora.api.player

import blora.api.command.CommandInvoker
import org.bukkit.entity.Player

interface QuickPlayer : CommandInvoker {

    override val asBukkit: Player

}