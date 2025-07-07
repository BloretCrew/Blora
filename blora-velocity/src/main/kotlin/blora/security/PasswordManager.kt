package blora.security

import blora.BloraPlugin
import blora.extension.containsIllegalCharacters
import blora.extension.localization
import blora.security.strategy.PasswordStrategy
import blora.security.strategy.PasswordStrategyResult
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

object PasswordManager {

    private val strategies = mutableMapOf<String, PasswordStrategy>()

    fun registerStrategy(id: String, strategy: PasswordStrategy) {
        if (strategies.containsKey(id)) {
            throw IllegalArgumentException("$id is already registered")
        }
        strategies[id] = strategy
    }

    fun securePassword(player: Player, password: String): PasswordStrategyResult {
        if (password.containsIllegalCharacters()) {
            return PasswordStrategyResult.Failure(
                component {
                    localization(player) {
                        this.warningRegisterPassword_strategy_failure_illegal_characters
                    }
                }
            )
        }
        if (BloraPlugin.configuration.security.weakPasswords.map { it.lowercase() }.contains(password.lowercase())) {
            return PasswordStrategyResult.Failure(
                component {
                    localization(player) {
                        this.warningRegisterPassword_strategy_failure_in_weak_password_dict
                    }
                }
            )
        }
        if (password.length < BloraPlugin.configuration.security.minPasswordLength ||
            password.length > BloraPlugin.configuration.security.maxPasswordLength
        ) {
            return PasswordStrategyResult.Failure(
                component {
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("max", BloraPlugin.configuration.security.maxPasswordLength.toString())
                            parsedPlaceholder("min", BloraPlugin.configuration.security.minPasswordLength.toString())
                        }
                    ) {
                        this.warningRegisterPassword_strategy_failure_length_not_secure
                    }
                }
            )
        }

        for (strategyId in this.listActivedStrategies()) {
            val (strategy: PasswordStrategy?, args: List<String>) = if (strategyId.contains(":")) {
                if (strategyId.indexOf(':') == strategyId.length - 1) {
                    strategies[strategyId.substring(0, strategyId.length - 1)] to emptyList()
                } else {
                    val split = strategyId.split(":")
                    val realId = split[0]
                    val args = split.subList(1, split.size).toList()
                    strategies[realId] to args
                }
            } else {
                strategies[strategyId] to emptyList()
            }
            if (strategy != null) {
                val result = strategy.secure(player, password, args)
                if (result != PasswordStrategyResult.Success)
                    return result
            }
        }
        return PasswordStrategyResult.Success
    }

    private fun listActivedStrategies(): List<String> {
        return BloraPlugin.configuration
            .security
            .passwordStrategy
            .toMutableList()
            .apply {
                this.add("charInclude")
            }
    }

}