package net.deechael.blora.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.GameProfileRequestEvent
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.util.GameProfile
import net.deechael.blora.BloraConstants
import net.deechael.blora.BloraPlugin
import net.deechael.blora.authorization.AuthDialog
import net.deechael.blora.authorization.BloraAuthorization
import net.deechael.blora.authorization.premium.PremiumAuthorizer
import net.deechael.blora.authorization.premium.PremiumPlayer
import net.deechael.blora.authorization.premium.fetcher.PremiumFetcher
import net.deechael.blora.config.Order
import net.deechael.blora.config.UUIDGenerator
import net.deechael.blora.database.BloraPlayer
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.time.LocalDate
import java.util.UUID

object BasicListener {

    private val premiumData: MutableMap<String, PremiumPlayer?> = mutableMapOf()

    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        // notice: when testing, comment the check below to avoid cannot do test
        if (event.server.serverInfo.name != BloraPlugin.limboServer.serverInfo.name)
            return

        if (BloraAuthorization.isAuthorized(event.player))
            return
        // how does a play is not authorized when they joined a server? send them to limbo!
        event.player.createConnectionRequest(BloraPlugin.limboServer).fireAndForget()

        // now database fetched player is always nonnull
        val databasePlayerByName = BloraPlugin.database.getPlayerByName(event.player.username)

        // show eula to the player if they haven't accepted the eula yet
        if (!databasePlayerByName!!.eulaAccepted) {
            AuthDialog.showEulaDialog(event.player) {
                AuthDialog.autoShowLoginDialog(it)
            }
        } else {
            AuthDialog.autoShowLoginDialog(event.player)
        }
    }

    // step 4
    @Subscribe(priority = -1000) // DO LAST
    fun onPostLogin(event: PostLoginEvent) {
        // caution: in this event, it's in CONFIGURATION stage, do not send packet in LOGIN or PLAY stage

        val ip = event.player.remoteAddress.address.hostAddress
        val username = event.player.username.lowercase()
        val uuid = event.player.uniqueId

        val premiumPlayer = this.premiumData[username]

        var databasePlayerByName = BloraPlugin.database.getPlayerByName(username)
        val databasePlayerByPremiumUuid = if (premiumPlayer != null) {
            BloraPlugin.database.getPlayerByPremiumUuid(premiumPlayer.uuid)
        } else {
            null
        }

        if (event.player.isOnlineMode) {
            if (premiumPlayer == null) { // why could this happen?
                event.player.disconnect(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickErrorProfiling
                    )
                )
                return
            }

            if (databasePlayerByName != null) {
                if (databasePlayerByName.premiumUuid == null || databasePlayerByPremiumUuid == null) {
                    event.player.disconnect(
                        BloraConstants.Objects.miniMessage.deserialize(
                            BloraPlugin.configuration.messages.loginKickSameNameOfflinePlayer
                        )
                    )
                    return
                }

                if (
                    databasePlayerByPremiumUuid.username != databasePlayerByName.username ||
                    databasePlayerByName.premiumUuid != premiumPlayer.uuid
                ) {
                    // this will happen when two of premium player swap username
                    event.player.disconnect(
                        BloraConstants.Objects.miniMessage.deserialize(
                            BloraPlugin.configuration.messages.loginKickSwapPremiumUsername
                        )
                    )
                    return
                }

                if (databasePlayerByName != databasePlayerByPremiumUuid) {
                    // last check to ensure that the player is correct player
                    event.player.disconnect(
                        BloraConstants.Objects.miniMessage.deserialize(
                            BloraPlugin.configuration.messages.loginKickErrorProfiling
                        )
                    )
                    return
                }

                BloraPlugin.database.trans {
                    databasePlayerByName.lastJoin = LocalDate.now()
                    databasePlayerByName.lastIp = ip
                }
            } else if (databasePlayerByPremiumUuid != null) {
                // premium player changed his/her username
                BloraPlugin.database.trans {
                    databasePlayerByPremiumUuid.username = username.lowercase()
                    databasePlayerByPremiumUuid.lastJoin = LocalDate.now()
                    databasePlayerByPremiumUuid.lastIp = ip
                    databasePlayerByPremiumUuid.flush()
                }
                databasePlayerByName = databasePlayerByPremiumUuid
            } else {
                // newly joined premium player
                BloraPlugin.database.trans {
                    databasePlayerByName = BloraPlayer.new {
                        this.uuid = uuid
                        this.premiumUuid = premiumPlayer.uuid
                        this.username = username.lowercase()
                        this.hashedPassword1 = "%unregistered%"
                        this.hashedPassword2 = "%unregistered%"
                        this.hashedPassword3 = "%unregistered%"
                        this.firstJoin = LocalDate.now()
                        this.lastJoin = LocalDate.now()
                        this.firstIp = ip
                        this.lastIp = ip
                        this.lastServer = BloraPlugin.configuration.server.lobby
                        this.email = null
                        this.autoLogin = false
                        this.eulaAccepted = false
                    }

                    databasePlayerByName.flush()
                }
            }
        } else {
            if (premiumPlayer != null) {
                event.player.disconnect(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickOnlineProfileButOfflineJoin
                    )
                )
                return
            } else if (databasePlayerByName == null) {
                // newly joined crack player
                BloraPlugin.database.trans {
                    databasePlayerByName = BloraPlayer.new {
                        this.uuid = uuid
                        this.premiumUuid = null
                        this.username = username.lowercase()
                        this.hashedPassword1 = "%unregistered%"
                        this.hashedPassword2 = "%unregistered%"
                        this.hashedPassword3 = "%unregistered%"
                        this.firstJoin = LocalDate.now()
                        this.lastJoin = LocalDate.now()
                        this.firstIp = ip
                        this.lastIp = ip
                        this.lastServer = BloraPlugin.configuration.server.lobby
                        this.email = null
                        this.autoLogin = false
                        this.eulaAccepted = false
                    }

                    databasePlayerByName.flush()
                }
            } else {
                BloraPlugin.database.trans {
                    databasePlayerByName.lastJoin = LocalDate.now()
                    databasePlayerByName.lastIp = ip
                    databasePlayerByName.flush()
                }
            }

            // send player to limbo server and wait for logging in, only crack player needs this
            event.player.createConnectionRequest(BloraPlugin.limboServer).fireAndForget()
        }

        // join player to authorization list
        BloraAuthorization.join(event.player)

        // if this player will not be kicked, still remove data but immediately because this player will stay on server
        // we shouldn't leave the useless data in memory to waste memory usage
        this.premiumData.remove(event.player.username.lowercase())
    }

    // step 3
    @Subscribe
    fun onLogin(event: LoginEvent) {
        // here to process to check if player online login success
        
    }

    // step 2
    @Subscribe
    fun onGameProfileRequest(event: GameProfileRequestEvent) {
        val original = event.gameProfile;
        val premiumPlayer = this.premiumData[original.name.lowercase()]

        val uuid = if (BloraPlugin.configuration.authorization.uuidGenerator == UUIDGenerator.MOJANG && premiumPlayer != null) {
            premiumPlayer.uuid
        } else {
            UUID.nameUUIDFromBytes(("OfflinePlayer:${original.name.lowercase()}").toByteArray(Charsets.UTF_8))
        }

        event.setGameProfile(GameProfile(uuid, original.name, original.properties));
    }

    // step 1
    @Subscribe(priority = -1000) // DO LAST
    fun onPreLogin(event: PreLoginEvent) {
        if (!event.result.isAllowed)
            return

        val ip = event.connection.remoteAddress.address.hostAddress

        val username = event.username.lowercase()

        if (BloraPlugin.configuration.authorization.checkUsername) {
            // check username valid or not

            // check min length
            if (username.length < BloraPlugin.configuration.authorization.minUsernameLength) {
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickUsernameTooShort,
                        Placeholder.parsed("length", BloraPlugin.configuration.authorization.minUsernameLength.toString())
                    )
                )
                return
            }
            // check max length
            if (username.length > BloraPlugin.configuration.authorization.maxUsernameLength) {
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickUsernameTooLong,
                        Placeholder.parsed("length", BloraPlugin.configuration.authorization.maxUsernameLength.toString())
                    )
                )
                return
            }
            // check character valid or not
            if (!username.matches(BloraPlugin.configuration.authorization.usernameRegex.toRegex())) {
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickUsernameContainsInvalidCharacters,
                        Placeholder.parsed("regex", BloraPlugin.configuration.authorization.usernameRegex)
                    )
                )
                return
            }
        }

        // ip limit
        if (BloraPlugin.configuration.security.ipLimit > 0 && !BloraPlugin.configuration.security.ipLimitDisableLogin) {
            if (BloraAuthorization.getPlayersUnderIp(ip).size
                >= BloraPlugin.configuration.security.ipLimit) { // use >= because if add one more player will be larger than the limit
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickSameIpLoginOvercount
                    )
                )
                return
            }
        }

        // if online features enabled, to do online features, otherwise consider players as offline players
        if (BloraPlugin.configuration.authorization.onlineFeatures) {
            val fetchResult = PremiumAuthorizer.fetchUserByName(username)

            // prepare uuid
            val newUuid = if (BloraPlugin.configuration.authorization.uuidGenerator == UUIDGenerator.MOJANG
                && fetchResult is PremiumFetcher.FetchResult.Exists) {
                fetchResult.player.uuid
            } else {
                UUID.nameUUIDFromBytes(("OfflinePlayer:$username").toByteArray(Charsets.UTF_8))
            }

            if (fetchResult is PremiumFetcher.FetchResult.Exists) { // consider player as online players
                this.premiumData[username] = fetchResult.player

                // get to database player object to prevent online player changing their in game name
                val databasePlayerByName = BloraPlugin.database.getPlayerByName(username)
                val databasePlayerByPremiumUuid = BloraPlugin.database.getPlayerByPremiumUuid(fetchResult.player.uuid)

                if (databasePlayerByName != null && databasePlayerByPremiumUuid != null) {
                    // same name and premium uuid all exists
                    if (!databasePlayerByPremiumUuid.username.contentEquals(databasePlayerByName.username, ignoreCase = true)) {
                        // if premium uuid refered player and username refered player is not same
                        // the only situation is, A and B all joined the server before, A is online player and B is offline player
                        // B player's username is not registered on Mojang server (crack), A players' username is registered (premium)
                        // but A player changed his/her username to B's which isn't registered on Mojang server
                        // so the username now refers to a new premium player
                        event.result = PreLoginEvent.PreLoginComponentResult.denied(
                            BloraConstants.Objects.miniMessage.deserialize(
                                BloraPlugin.configuration.messages.loginKickSameNameOfflinePlayer
                            )
                        )
                        return
                    }
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode() // online player
                } else if (databasePlayerByPremiumUuid != null) { // means this premium player changed their ign and no offline player is using this name, which is great, it's easy to migrate!
                    BloraPlugin.database.trans {
                        databasePlayerByPremiumUuid.username = username
                        databasePlayerByPremiumUuid.flush() // save changes to database
                    }
                } else if (databasePlayerByName != null) {
                    // this player has registered their username under offline mode, just join the game as offline player
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                } else {
                    // other situations should check if this player is in online mode
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                }
            } else { // data not fetched, consider player as offline player
                this.premiumData[username] = null
                val databasePlayer = BloraPlugin.database.getPlayerByName(username)

                if (databasePlayer != null) {
                    if (databasePlayer.premiumUuid != null) { // a rarely happen event, but it will, it does exist!
                        event.result = PreLoginEvent.PreLoginComponentResult.denied(
                            BloraConstants.Objects.miniMessage.deserialize(
                                BloraPlugin.configuration.messages.loginKickSameOldNameOnlinePlayer
                            )
                        )
                        return
                    }
                } else {
                    // when database player not exists, do ip limit test
                    if (BloraPlugin.configuration.security.ipLimit > 0 && !BloraPlugin.configuration.security.ipLimitDisableRegister) {
                        val amount =
                            if (BloraPlugin.configuration.security.ipLimitStrategyForRegister == Order.FIRST) {
                                BloraPlugin.database.getFirstIpAmount(ip)
                            } else {
                                BloraPlugin.database.getLastIpAmount(ip)
                            }
                        if (amount >= BloraPlugin.configuration.security.ipLimit) { // use >= because if add one more player will be larger than the limit
                            event.result = PreLoginEvent.PreLoginComponentResult.denied(
                                BloraConstants.Objects.miniMessage.deserialize(
                                    BloraPlugin.configuration.messages.loginKickSameIpRegisterOvercount
                                )
                            )
                            return
                        }
                    }
                }

                event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode() // offline player
            }
        } else {
            event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
        }
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        BloraAuthorization.clear(event.player)
        this.premiumData.remove(event.player.username.lowercase())
        println("status: ${event.loginStatus}")
    }

    @Subscribe
    fun onKickedFromServer(event: KickedFromServerEvent) {
        BloraAuthorization.clear(event.player)
        this.premiumData.remove(event.player.username.lowercase())
        event.serverKickReason.ifPresent {
            println("reason: ${GsonComponentSerializer.gson().serialize(it)}")
        }
        val result = event.result
        if (result is KickedFromServerEvent.DisconnectPlayer) {
            println("kick: disconnect player: ${GsonComponentSerializer.gson().serialize(result.reasonComponent)}")
        } else if (result is KickedFromServerEvent.RedirectPlayer && result.messageComponent != null) {
            println("kick: redirect player: ${GsonComponentSerializer.gson().serialize(result.messageComponent!!)}")
        } else if (result is KickedFromServerEvent.Notify) {
            println("kick: notify: ${GsonComponentSerializer.gson().serialize(result.messageComponent)}")
        }
    }

}