package net.deechael.blora.authorization

import com.velocitypowered.api.proxy.Player
import net.deechael.blora.BloraPlugin
import net.deechael.blora.dialog.builtin.eulaDialog
import net.deechael.blora.dialog.builtin.loginDialog
import net.deechael.blora.dialog.builtin.registerDialog
import net.deechael.blora.extension.notNull
import net.deechael.blora.security.PasswordHasher
import net.deechael.blora.security.PasswordManager
import net.deechael.blora.security.strategy.PasswordStrategyResult
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini

object AuthDialog {

    fun autoShowLoginDialog(player: Player) {
        if (player.isOnlineMode && BloraPlugin.configuration.security.allowOnlinePlayerAutoLogin) {
            BloraPlugin.database.getPlayerByName(player.username).notNull {
                player.createConnectionRequest(
                    BloraPlugin.proxyServer
                        .getServer(this.lastServer)
                        .orElse(BloraPlugin.lobbyServer)
                ).fireAndForget()
            }
        } else {
            val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!
            if (databasePlayer.hashedPassword1 != "%unregistered%") {
                showLoginDialog(player)
            } else {
                showRegisterDialog(player)
            }
        }
    }

    fun showEulaDialog(player: Player, then: (player: Player) -> Unit) {
        val dialog = eulaDialog()
        // TODO: send dialog
    }

    fun showLoginDialog(player: Player, warningMessage: Component? = null) {
        val dialog = loginDialog(warningMessage)
        // TODO: send dialog
    }

    fun showRegisterDialog(player: Player, warningMessage: Component? = null) {
        val dialog = registerDialog(warningMessage)
        // TODO: send dialog
    }

    fun eulaDialogCallback(player: Player, accepted: Boolean, then: (Player) -> Unit) {
        if (accepted) {
            val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!
            BloraPlugin.database.trans {
                databasePlayer.eulaAccepted = true
                databasePlayer.flush()
            }
            then(player)
        } else {
            player.disconnect(component {
                mini(BloraPlugin.configuration.messages.ingameKickNotAcceptEULA)
            })
        }
    }

    fun loginDialogCallback(player: Player, value: String) {
        BloraPlugin.database.getPlayerByName(player.username).notNull {
            if (PasswordHasher.hash(value) != this.hashedPassword()) {
                if (BloraPlugin.configuration.security.maxRetries > 0
                    && BloraAuthorization.passwordRetries
                        .getOrDefault(player, 0) > BloraPlugin.configuration.security.maxRetries
                ) {
                    player.disconnect(component {
                        mini(BloraPlugin.configuration.messages.ingameKickTooManyRetries)
                    })
                } else {
                    showLoginDialog(player, component {
                        mini(BloraPlugin.configuration.messages.loginWarningLoginPasswordIncorrect)
                    })
                }
            } else {
                BloraAuthorization.authorize(player)
                player.createConnectionRequest(
                    BloraPlugin.proxyServer
                        .getServer(this.lastServer)
                        .orElse(BloraPlugin.lobbyServer)
                ).fireAndForget()
            }
        }
    }

    fun registerDialogCallback(player: Player, password: String, confirm: String) {
        if (password != confirm) {
            showRegisterDialog(player, component {
                mini(BloraPlugin.configuration.messages.loginWarningLoginPasswordIncorrect)
            })
        } else {
            val result = PasswordManager.securePassword(player, password)
            if (result != PasswordStrategyResult.Success) {
                result as PasswordStrategyResult.Failure
                showRegisterDialog(player, result.reason)
            } else {
                val (hash1, hash2, hash3) = PasswordHasher.hash(password)
                val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!
                BloraPlugin.database.trans {
                    databasePlayer.hashedPassword1 = hash1
                    databasePlayer.hashedPassword2 = hash2
                    databasePlayer.hashedPassword3 = hash3
                    databasePlayer.flush()
                }
                BloraAuthorization.authorize(player)
                player.createConnectionRequest(BloraPlugin.lobbyServer).fireAndForget()
            }
        }
    }

}