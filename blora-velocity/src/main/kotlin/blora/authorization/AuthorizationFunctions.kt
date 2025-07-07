package blora.authorization

import blora.BloraPlugin
import blora.dialog.asPacket
import blora.eula.eulaDialog
import blora.extension.disconnect
import blora.extension.localization
import blora.extension.notNull
import blora.extension.sendPacket
import blora.options.OptionStatus
import blora.security.PasswordHasher
import blora.security.PasswordManager
import blora.security.strategy.PasswordStrategyResult
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component

object AuthorizationFunctions {

    fun transferPlayerToSuitableServer(player: Player) {
        val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!

        fun enable() {
            val currentServer = player.currentServer
            if (currentServer != null) {
                // to prevent showing "You have already connected to the server"
                if (currentServer.get().serverInfo.name != BloraPlugin.lobbyServer.serverInfo.name) {
                    player.createConnectionRequest(BloraPlugin.lobbyServer).fireAndForget()
                }
            } else {
                player.createConnectionRequest(BloraPlugin.lobbyServer).fireAndForget()
            }
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
        if (BloraAuthorization.isAuthorized(player)) {
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
        player.sendPacket(eulaDialog(player).asPacket())
    }

    fun showLoginDialog(player: Player, warningMessage: Component? = null) {
        player.sendPacket(loginDialog(player, warningMessage).asPacket())
    }

    fun showRegisterDialog(player: Player, warningMessage: Component? = null) {
        player.sendPacket(registerDialog(player, warningMessage).asPacket())
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
            player.disconnect {
                localization(player) {
                    this.kickIngameNot_accept_eula
                }
            }
        }
    }

    fun loginDialogCallback(player: Player, value: String) {
        BloraPlugin.database.getPlayerByName(player.username).notNull {
            if (PasswordHasher.hash(value) != this.hashedPassword()) {
                if (BloraPlugin.configuration.security.maxRetries > 0
                    && BloraAuthorization.passwordRetries
                        .getOrDefault(player, 0) > BloraPlugin.configuration.security.maxRetries
                ) {
                    player.disconnect {
                        localization(player) {
                            this.kickLoginToo_many_retries
                        }
                    }
                } else {
                    showLoginDialog(player, component {
                        localization(player) {
                            this.warningLoginPassword_incorrect
                        }
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
                localization(player) {
                    this.warningRegisterConfirm_not_same
                }
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