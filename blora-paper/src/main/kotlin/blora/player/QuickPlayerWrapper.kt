package blora.player

import blora.command.CommandInvokerWrapper
import org.bukkit.entity.Player

class QuickPlayerWrapper(
    private val player: Player
) : CommandInvokerWrapper(player), blora.api.player.BloraPlayer {

    override val isPlayer: Boolean
        get() = true
    override val isBlock: Boolean
        get() = false
    override val asBukkit: Player
        get() = this.player

}