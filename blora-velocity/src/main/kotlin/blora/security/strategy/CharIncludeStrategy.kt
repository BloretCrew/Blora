package blora.security.strategy

import blora.BloraPlugin
import blora.extension.containsNumber
import blora.extension.containsSymbol
import blora.extension.containsUppercase
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini

object CharIncludeStrategy : PasswordStrategy {

    override fun secure(
        player: Player,
        password: String,
        args: List<String>
    ): PasswordStrategyResult {
        val strategies = BloraPlugin.configuration.security.passwordStrategy.map { it.lowercase() }

        val uppercase = strategies.contains("uppercaseincluded")
        val number = strategies.contains("numberincluded")
        val symbol = strategies.contains("symbolincluded")

        if (!uppercase && !number && !symbol) {
            return PasswordStrategyResult.Success
        }

        val message = if (uppercase) {
            if (number) {
                if (symbol) {
                    BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureUppercaseAndNumberAndSymbolIncluded
                } else {
                    BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureNumberAndUppercaseIncluded
                }
            } else {
                if (symbol) {
                    BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureUppercaseAndSymbolIncluded
                } else {
                    BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureUppercaseIncluded
                }
            }
        } else {
            if (number) {
                if (symbol) {
                    BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureNumberAndSymbolIncluded
                } else {
                    BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureNumberIncluded
                }
            } else {
                BloraPlugin.configuration.messages.loginWarningRegisterPasswordStrategyFailureSymbolIncluded
            }
        }

        val failure = PasswordStrategyResult.Failure(component {
            mini(message)
        })

        if (uppercase) {
            if (!password.containsUppercase()) {
                return failure
            }
        }
        if (number) {
            if (!password.containsNumber()) {
                return failure
            }
        }
        if (symbol) {
            if (!password.containsSymbol()) {
                return failure
            }
        }

        return PasswordStrategyResult.Success
    }

}