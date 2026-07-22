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
        BloraPlugin.log.info("[LOGIN SYSTEM] Transferring player to suitable server")
        val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!

        fun enable() {
            BloraPlugin.log.info("[LOGIN SYSTEM] Doing always lobby logic")
            if (BloraAuthorization.isAuthorized(player)) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player authorized, transferring to gaming server")
                val currentServer = player.currentServer
                if (currentServer != null) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player current server is not null, checking if player is in the lobby server")
                    // to prevent showing "You have already connected to the server"
                    if (currentServer.get().serverInfo.name != BloraPlugin.lobbyServer.serverInfo.name) {
                        player.createConnectionRequest(BloraPlugin.lobbyServer).fireAndForget()
                    }
                } else {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player current server is null, transferring player to lobby")
                    player.createConnectionRequest(BloraPlugin.lobbyServer).fireAndForget()
                }
            } else {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player unauthorized, transferring to limbo")
                player.createConnectionRequest(BloraPlugin.limboServer).fireAndForget()
            }
        }

        fun disable() {
            BloraPlugin.log.info("[LOGIN SYSTEM] Doing last lobby logic")
            if (BloraAuthorization.isAuthorized(player)) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player authorized, transferring to gaming server")
                val currentServer = player.currentServer
                if (currentServer.isPresent) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player current server is not null, checking if player is in the correct server")
                    if (currentServer.get().server.serverInfo.name != databasePlayer.lastServer) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player isn't in the correct server, transferring")
                        player.createConnectionRequest(
                            BloraPlugin.proxyServer
                                .getServer(databasePlayer.lastServer)
                                .orElse(BloraPlugin.lobbyServer)
                        ).fireAndForget()
                    }
                } else {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player current server is null, transferring player to last server or fallback lobby")
                    player.createConnectionRequest(
                        BloraPlugin.proxyServer
                            .getServer(databasePlayer.lastServer)
                            .orElse(BloraPlugin.lobbyServer)
                    ).fireAndForget()
                }
            } else {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player unauthorized, transferring to limbo")
                player.createConnectionRequest(BloraPlugin.limboServer).fireAndForget()
            }
        }

        when (databasePlayer.jsonOptions.alwaysLobby) {
            OptionStatus.ENABLE -> {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player turned on always lobby")
                enable()
            }

            OptionStatus.DISABLE -> {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player turned off always lobby")
                disable()
            }

            OptionStatus.NOT_SET -> {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player not set always lobby")
                if (BloraPlugin.configuration.authorization.alwaysLobby) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Server turned on always lobby")
                    enable()
                } else {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Server turned off always lobby")
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

    fun showChangePasswordDialog(player: Player, warningMessage: Component? = null) {
        player.sendPacket(changePasswordDialog(player, warningMessage).asPacket())
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
                val retries = (BloraAuthorization.passwordRetries.getOrDefault(player, 0) + 1).also {
                    BloraAuthorization.passwordRetries[player] = it
                }
                if (BloraPlugin.configuration.security.maxRetries > 0
                    && retries > BloraPlugin.configuration.security.maxRetries
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
                BloraAuthorization.passwordRetries.remove(player)
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

    fun changePasswordDialogCallback(
        player: Player,
        oldPassword: String,
        newPassword: String,
        confirmPassword: String,
    ) {
        if (!BloraAuthorization.isAuthorized(player)) {
            return
        }
        val databasePlayer = BloraPlugin.database.getPlayerByName(player.username) ?: return
        if (databasePlayer.hashedPassword1 == "%unregistered%") {
            // Should rarely reach here (command already gates this); keep premium-aware message.
            player.sendMessage(
                component {
                    localization(player) {
                        if (player.isOnlineMode || databasePlayer.premiumUuid != null) {
                            this.commandErrorChangepassword_premium_no_password
                        } else {
                            this.commandErrorChangepassword_not_registered
                        }
                    }
                }
            )
            return
        }
        if (PasswordHasher.hash(oldPassword) != databasePlayer.hashedPassword()) {
            showChangePasswordDialog(player, component {
                localization(player) {
                    this.warningChangepassword_old_incorrect
                }
            })
            return
        }
        if (newPassword != confirmPassword) {
            showChangePasswordDialog(player, component {
                localization(player) {
                    this.warningRegisterConfirm_not_same
                }
            })
            return
        }
        if (oldPassword == newPassword) {
            showChangePasswordDialog(player, component {
                localization(player) {
                    this.warningChangepassword_same_as_old
                }
            })
            return
        }
        val result = PasswordManager.securePassword(player, newPassword)
        if (result != PasswordStrategyResult.Success) {
            result as PasswordStrategyResult.Failure
            showChangePasswordDialog(player, result.reason)
            return
        }
        val (hash1, hash2, hash3) = PasswordHasher.hash(newPassword)
        BloraPlugin.database.trans {
            databasePlayer.hashedPassword1 = hash1
            databasePlayer.hashedPassword2 = hash2
            databasePlayer.hashedPassword3 = hash3
            databasePlayer.flush()
        }
        player.sendMessage(
            component {
                localization(player) {
                    this.commandSuccessChangepassword
                }
            }
        )
    }

}