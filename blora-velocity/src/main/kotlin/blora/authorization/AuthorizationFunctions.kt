package blora.authorization

import blora.BloraPlugin
import blora.dialog.asPacket
import blora.eula.eulaDialog
import blora.extension.disconnect
import blora.extension.localization
import blora.extension.notNull
import blora.extension.sendPacket
import blora.database.player.PlayerTable
import blora.options.OptionStatus
import blora.security.PasswordManager
import blora.security.SecurePasswordHasher
import blora.security.strategy.PasswordStrategyResult
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
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
            AuthFlow.clear(player)
            this.transferPlayerToSuitableServer(player)
        } else {
            val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!
            if (!SecurePasswordHasher.isUnregistered(
                    databasePlayer.hashedPassword1,
                    databasePlayer.hashedPassword2,
                    databasePlayer.hashedPassword3
                )
            ) {
                showLoginDialog(player)
            } else {
                showRegisterDialog(player)
            }
        }
    }

    fun showEulaDialog(player: Player) {
        AuthFlow.open(player, AuthDialogKind.EULA)
        player.sendPacket(eulaDialog(player).asPacket())
    }

    fun showLoginDialog(player: Player, warningMessage: Component? = null) {
        AuthFlow.open(player, AuthDialogKind.LOGIN)
        player.sendPacket(loginDialog(player, warningMessage).asPacket())
    }

    fun showRegisterDialog(player: Player, warningMessage: Component? = null) {
        AuthFlow.open(player, AuthDialogKind.REGISTER)
        player.sendPacket(registerDialog(player, warningMessage).asPacket())
    }

    fun showChangePasswordDialog(player: Player, warningMessage: Component? = null) {
        AuthFlow.open(player, AuthDialogKind.CHANGE_PASSWORD)
        player.sendPacket(changePasswordDialog(player, warningMessage).asPacket())
    }

    fun eulaDialogCallback(player: Player, accepted: Boolean) {
        if (!AuthFlow.consume(player, AuthDialogKind.EULA)) {
            BloraPlugin.log.warn("[SECURITY] Rejected EULA action from ${player.username} (bad auth flow)")
            return
        }
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
        if (BloraAuthorization.isAuthorized(player)) {
            return
        }
        if (!AuthFlow.consume(player, AuthDialogKind.LOGIN)) {
            BloraPlugin.log.warn("[SECURITY] Rejected login action from ${player.username} (bad auth flow)")
            showLoginDialog(player)
            return
        }
        BloraPlugin.database.getPlayerByName(player.username).notNull {
            if (!SecurePasswordHasher.verify(
                    value,
                    this.hashedPassword1,
                    this.hashedPassword2,
                    this.hashedPassword3
                )
            ) {
                val retries = (BloraAuthorization.passwordRetries.getOrDefault(player, 0) + 1).also {
                    BloraAuthorization.passwordRetries[player] = it
                }
                // "max N failures" → kick when retries >= maxRetries
                if (BloraPlugin.configuration.security.maxRetries > 0
                    && retries >= BloraPlugin.configuration.security.maxRetries
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
                // Upgrade legacy hash on successful login.
                if (SecurePasswordHasher.needsUpgrade(this.hashedPassword2, this.hashedPassword3)) {
                    val (h1, h2, h3) = SecurePasswordHasher.hashNew(value)
                    BloraPlugin.database.trans {
                        this@notNull.hashedPassword1 = h1
                        this@notNull.hashedPassword2 = h2
                        this@notNull.hashedPassword3 = h3
                        this@notNull.flush()
                    }
                }
                BloraAuthorization.passwordRetries.remove(player)
                AuthFlow.clear(player)
                BloraAuthorization.authorize(player)
                transferPlayerToSuitableServer(player)
            }
        }
    }

    fun registerDialogCallback(player: Player, password: String, confirm: String) {
        if (BloraAuthorization.isAuthorized(player)) {
            BloraPlugin.log.warn("[SECURITY] Rejected register from authorized player ${player.username}")
            return
        }
        if (!AuthFlow.consume(player, AuthDialogKind.REGISTER)) {
            BloraPlugin.log.warn("[SECURITY] Rejected register action from ${player.username} (bad auth flow)")
            // Do not open register for already-registered accounts.
            autoShowLoginDialog(player)
            return
        }
        if (password != confirm) {
            showRegisterDialog(player, component {
                localization(player) {
                    this.warningRegisterConfirm_not_same
                }
            })
            return
        }
        val result = PasswordManager.securePassword(player, password)
        if (result != PasswordStrategyResult.Success) {
            result as PasswordStrategyResult.Failure
            showRegisterDialog(player, result.reason)
            return
        }
        val (hash1, hash2, hash3) = SecurePasswordHasher.hashNew(password)
        val username = player.username.lowercase()
        val updated = BloraPlugin.database.trans {
            // Atomic: only set password when still fully unregistered.
            PlayerTable.update({
                (PlayerTable.username eq username) and
                    (PlayerTable.hashedPassword1 eq SecurePasswordHasher.UNREGISTERED) and
                    (PlayerTable.hashedPassword2 eq SecurePasswordHasher.UNREGISTERED) and
                    (PlayerTable.hashedPassword3 eq SecurePasswordHasher.UNREGISTERED)
            }) {
                it[hashedPassword1] = hash1
                it[hashedPassword2] = hash2
                it[hashedPassword3] = hash3
            }
        }
        if (updated != 1) {
            BloraPlugin.log.warn(
                "[SECURITY] Register rejected for ${player.username}: account already has a password or missing row"
            )
            showLoginDialog(player)
            return
        }
        AuthFlow.clear(player)
        BloraAuthorization.authorize(player)
        transferPlayerToSuitableServer(player)
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
        if (!AuthFlow.consume(player, AuthDialogKind.CHANGE_PASSWORD)) {
            BloraPlugin.log.warn("[SECURITY] Rejected change-password action from ${player.username}")
            return
        }
        val databasePlayer = BloraPlugin.database.getPlayerByName(player.username) ?: return
        if (SecurePasswordHasher.isUnregistered(
                databasePlayer.hashedPassword1,
                databasePlayer.hashedPassword2,
                databasePlayer.hashedPassword3
            )
        ) {
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
        if (!SecurePasswordHasher.verify(
                oldPassword,
                databasePlayer.hashedPassword1,
                databasePlayer.hashedPassword2,
                databasePlayer.hashedPassword3
            )
        ) {
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
        val strategy = PasswordManager.securePassword(player, newPassword)
        if (strategy != PasswordStrategyResult.Success) {
            strategy as PasswordStrategyResult.Failure
            showChangePasswordDialog(player, strategy.reason)
            return
        }
        val (hash1, hash2, hash3) = SecurePasswordHasher.hashNew(newPassword)
        BloraPlugin.database.trans {
            databasePlayer.hashedPassword1 = hash1
            databasePlayer.hashedPassword2 = hash2
            databasePlayer.hashedPassword3 = hash3
            databasePlayer.flush()
        }
        AuthFlow.clear(player)
        player.sendMessage(
            component {
                localization(player) {
                    this.commandSuccessChangepassword
                }
            }
        )
    }

}