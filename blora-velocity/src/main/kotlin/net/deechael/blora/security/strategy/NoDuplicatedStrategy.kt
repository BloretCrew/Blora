package net.deechael.blora.security.strategy

import com.velocitypowered.api.proxy.Player
import net.deechael.blora.BloraPlugin
import net.deechael.blora.extension.hasRepeatedCharRegex
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.parsedPlaceholder

object NoDuplicatedStrategy : PasswordStrategy {

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
                    mini(BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureNoDuplicated) {
                        parsedPlaceholder("limit", times.toString())
                    }
                }
            )
        } else {
            PasswordStrategyResult.Success
        }
    }

}