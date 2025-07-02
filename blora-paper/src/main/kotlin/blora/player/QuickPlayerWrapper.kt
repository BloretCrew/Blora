package blora.player

import org.bukkit.entity.Player
import blora.command.CommandInvokerWrapper

class QuickPlayerWrapper(
    private val player: Player
) : CommandInvokerWrapper(player), blora.api.player.QuickPlayer {

    override val isPlayer: Boolean
        get() = true
    override val isBlock: Boolean
        get() = false
    override val asBukkit: Player
        get() = this.player

}