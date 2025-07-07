package blora.security.strategy

import blora.extension.localization
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.component

object NoUsernameStrategy : PasswordStrategy {

    override fun secure(
        player: Player,
        password: String,
        args: List<String>
    ): PasswordStrategyResult {
        return if (password.lowercase().contains(player.username.lowercase())) {
            PasswordStrategyResult.Failure(
                component {
                    localization(player) {
                        this.warningRegisterPassword_strategy_failure_no_username
                    }
                }
            )
        } else {
            PasswordStrategyResult.Success
        }
    }

}