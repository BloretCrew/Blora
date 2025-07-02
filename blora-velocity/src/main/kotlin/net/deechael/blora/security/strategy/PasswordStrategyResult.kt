package net.deechael.blora.security.strategy

import net.kyori.adventure.text.Component

sealed class PasswordStrategyResult {

    object Success : PasswordStrategyResult()
    class Failure(val reason: Component) : PasswordStrategyResult()

}