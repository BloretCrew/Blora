package net.deechael.blora.authorization

import com.velocitypowered.api.proxy.Player
import net.deechael.blora.BloraPlugin
import net.deechael.blora.dialog.asPacket
import net.deechael.blora.dialog.builtin.eulaDialog
import net.deechael.blora.dialog.builtin.loginDialog
import net.deechael.blora.dialog.builtin.registerDialog
import net.deechael.blora.extension.notNull
import net.deechael.blora.extension.sendPacket
import net.deechael.blora.options.OptionStatus
import net.deechael.blora.security.PasswordHasher
import net.deechael.blora.security.PasswordManager
import net.deechael.blora.security.strategy.PasswordStrategyResult
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini

object AuthorizationFunctions {

    fun transferPlayerToSuitableServer(player: Player) {
        val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!

        fun enable() {
            player.createConnectionRequest(BloraPlugin.lobbyServer).fireAndForget()
        }

        fun disable() {
            if (BloraAuthorization.isAuthorized(player)) {
                val currentServer = player.currentServer
                if (currentServer.isPresent) {
                    if (currentServer.get().server.serverInfo.name != databasePlayer.lastServer) {
                        player.createConnectionRequest(
                            BloraPlugin.proxyServer
                                .getServer(databasePlayer.lastServer)
                                .orElse(BloraPlugin.lobbyServer)
                        ).fireAndForget()
                    } else {
                        player.createConnectionRequest(BloraPlugin.lobbyServer).fireAndForget()
                    }
                } else {
                    player.createConnectionRequest(
                        BloraPlugin.proxyServer
                            .getServer(databasePlayer.lastServer)
                            .orElse(BloraPlugin.lobbyServer)
                    ).fireAndForget()
                }
            } else {
                player.createConnectionRequest(BloraPlugin.limboServer).fireAndForget()
            }
        }

        when (databasePlayer.jsonOptions.alwaysLobby) {
            OptionStatus.ENABLE -> {
                enable()
            }

            OptionStatus.DISABLE -> {
                disable()
            }

            OptionStatus.NOT_SET -> {
                if (BloraPlugin.configuration.authorization.alwaysLobby) {
                    enable()
                } else {
                    disable()
                }
            }
        }
    }

    fun autoShowLoginDialog(player: Player) {
        if (player.isOnlineMode || BloraAuthorization.isAuthorized(player)) {
            this.transferPlayerToSuitableServer(player)
        } else {
            val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!
            if (databasePlayer.hashedPassword1 != "%unregistered%") {
                showLoginDialog(player)
            } else {
                showRegisterDialog(player)
            }
        }
    }

    fun showEulaDialog(player: Player) {
        player.sendPacket(eulaDialog().asPacket())
    }

    fun showLoginDialog(player: Player, warningMessage: Component? = null) {
        player.sendPacket(loginDialog(warningMessage).asPacket())
    }

    fun showRegisterDialog(player: Player, warningMessage: Component? = null) {
        player.sendPacket(registerDialog(warningMessage).asPacket())
    }

    fun eulaDialogCallback(player: Player, accepted: Boolean) {
        if (accepted) {
            val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!
            BloraPlugin.database.trans {
                databasePlayer.eulaAccepted = true
                databasePlayer.flush()
            }
            autoShowLoginDialog(player)
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
                transferPlayerToSuitableServer(player)
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
                transferPlayerToSuitableServer(player)
            }
        }
    }

}