package blora.security.strategy

import blora.BloraPlugin
import com.velocitypowered.api.proxy.Player
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