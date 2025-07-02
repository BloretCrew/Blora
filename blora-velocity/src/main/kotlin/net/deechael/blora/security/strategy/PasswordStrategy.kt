package net.deechael.blora.security.strategy

import com.velocitypowered.api.proxy.Player

interface PasswordStrategy {

    fun secure(player: Player, password: String, args: List<String>): PasswordStrategyResult

}