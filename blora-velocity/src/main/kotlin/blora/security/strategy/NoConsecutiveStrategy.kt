package blora.security.strategy

import blora.extension.hasRepeatedCharRegex
import blora.extension.localization
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

object NoConsecutiveStrategy : PasswordStrategy {

    override fun secure(
        player: Player,
        password: String,
        args: List<String>
    ): PasswordStrategyResult {
        if (args.isEmpty())
            return PasswordStrategyResult.Success
        val times = args[0].toIntOrNull()
        if (times == null || times <= 1)
            return PasswordStrategyResult.Success
        return if (password.hasRepeatedCharRegex(times)) {
            PasswordStrategyResult.Failure(
                component {
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("limit", times.toString())
                        }
                    ) {
                        this.warningRegisterPassword_strategy_failure_no_consecutive
                    }
                }
            )
        } else {
            PasswordStrategyResult.Success
        }
    }

}