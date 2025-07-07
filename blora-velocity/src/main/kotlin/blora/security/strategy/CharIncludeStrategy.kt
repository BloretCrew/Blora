package blora.security.strategy

import blora.BloraPlugin
import blora.extension.containsNumber
import blora.extension.containsSymbol
import blora.extension.containsUppercase
import blora.extension.localization
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.component

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


        val failure = PasswordStrategyResult.Failure(component {
            localization(player) {
                if (uppercase) {
                    if (number) {
                        if (symbol) {
                            this.warningRegisterPassword_strategy_failure_uppercase_and_number_and_symbol_included
                        } else {
                            this.warningRegisterPassword_strategy_failure_number_and_uppercase_included
                        }
                    } else {
                        if (symbol) {
                            this.warningRegisterPassword_strategy_failure_uppercase_and_symbol_included
                        } else {
                            this.warningRegisterPassword_strategy_failure_uppercase_included
                        }
                    }
                } else {
                    if (number) {
                        if (symbol) {
                            this.warningRegisterPassword_strategy_failure_number_and_symbol_included
                        } else {
                            this.warningRegisterPassword_strategy_failure_number_included
                        }
                    } else {
                        this.warningRegisterPassword_strategy_failure_symbol_included
                    }
                }
            }
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