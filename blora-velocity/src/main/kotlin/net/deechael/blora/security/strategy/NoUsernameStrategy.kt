package net.deechael.blora.security.strategy

import com.velocitypowered.api.proxy.Player
import net.deechael.blora.BloraPlugin
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini

object NoUsernameStrategy : PasswordStrategy {

    override fun secure(
        player: Player,
        password: String,
        args: List<String>
    ): PasswordStrategyResult {
        return if (password.lowercase().contains(player.username.lowercase())) {
            PasswordStrategyResult.Failure(
                component {
                    mini(BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureNoUsername)
                }
            )
        } else {
            PasswordStrategyResult.Success
        }
    }

}