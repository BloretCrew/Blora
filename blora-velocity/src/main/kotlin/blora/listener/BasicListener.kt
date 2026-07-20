package blora.listener

import blora.BloraPlugin
import blora.authorization.AuthorizationFunctions
import blora.authorization.BloraAuthorization
import blora.command.PrivateMessageSpy
import blora.authorization.premium.PremiumAuthorizer
import blora.authorization.premium.PremiumPlayer
import blora.authorization.premium.fetcher.PremiumFetcher
import blora.configuration.Order
import blora.configuration.UUIDGenerator
import blora.database.player.PlayerDao
import blora.extension.disconnect
import blora.extension.localization
import blora.options.OptionStatus
import blora.options.OptionsFunctions
import blora.options.PlayerOptions
import blora.protocol.packet.CustomClickAction
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.GameProfileRequestEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.player.configuration.PlayerFinishedConfigurationEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.util.GameProfile
import io.github._4drian3d.vpacketevents.api.event.PacketReceiveEvent
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object BasicListener {

    private val premiumData: MutableMap<String, PremiumPlayer?> = mutableMapOf()
    private val passedLoginStatus: MutableList<Player> = mutableListOf()

    @Subscribe
    fun onPacketReceive(event: PacketReceiveEvent) {
        val packet = event.packet
        if (packet is CustomClickAction) {
            if (packet.id == "minecraft:blora_eula_accept") {
                AuthorizationFunctions.eulaDialogCallback(event.player, true)
            } else if (packet.id == "minecraft:blora_eula_reject") {
                AuthorizationFunctions.eulaDialogCallback(event.player, false)
            } else if (packet.id == "minecraft:blora_login") {
                AuthorizationFunctions.loginDialogCallback(
                    event.player,
                    ((packet.payload as NbtCompound)["blora_password"] as NbtString).value
                )
            } else if (packet.id == "minecraft:blora_register") {
                AuthorizationFunctions.registerDialogCallback(
                    event.player,
                    ((packet.payload as NbtCompound)["blora_password"] as NbtString).value,
                    ((packet.payload as NbtCompound)["blora_confirm_password"] as NbtString).value
                )
            } else if (packet.id == "minecraft:blora_exit") {
                if (!BloraPlugin.configuration.administration.debug) {
                    event.player.disconnect {
                        localization(event.player) {
                            this.kickLoginExit
                        }
                    }
                }
            } else if (packet.id == "minecraft:blora_player_options_exit") {
                OptionsFunctions.updatePlayerOptions(event.player, packet.payload as NbtCompound)
            }
        }

    }

    @Subscribe
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        if (this.passedLoginStatus.contains(event.player))
            return
        this.passedLoginStatus.add(event.player)
        if (BloraAuthorization.isAuthorized(event.player)) {
            val databasePlayer = BloraPlugin.database.getPlayerByName(event.player.username)!!
            if (databasePlayer.jsonOptions.alwaysLobby == OptionStatus.ENABLE || (databasePlayer.jsonOptions.alwaysLobby == OptionStatus.NOT_SET && BloraPlugin.configuration.authorization.alwaysLobby)) {
                event.result = ServerPreConnectEvent.ServerResult.allowed(BloraPlugin.lobbyServer)
            } else {
                event.result = ServerPreConnectEvent.ServerResult.allowed(
                    BloraPlugin.proxyServer
                        .getServer(databasePlayer.lastServer)
                        .orElse(BloraPlugin.lobbyServer)
                )
            }
            return
        }
        if (event.originalServer.serverInfo.name == BloraPlugin.limboServer.serverInfo.name)
            return
        event.result = ServerPreConnectEvent.ServerResult.allowed(BloraPlugin.limboServer)
    }

    @Subscribe
    fun onFinishedConfiguration(event: PlayerFinishedConfigurationEvent) {
        BloraPlugin.proxyServer.scheduler.buildTask(BloraPlugin.instance) { task ->
            BloraPlugin.database.playerLoginDateSave(event.player.uniqueId)
            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} is connecting to server ${event.server.serverInfo.name}")
            // notice: when testing, comment the check below to avoid cannot do test
            val databasePlayer = BloraPlugin.database.getPlayerByName(event.player.username)

            if (BloraAuthorization.isAuthorized(event.player)) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} is authorized, stay in their server")
                if (!databasePlayer!!.eulaAccepted) {
                    AuthorizationFunctions.showEulaDialog(event.player)
                }
                return@buildTask
            }

            if (event.server.serverInfo.name != BloraPlugin.limboServer.serverInfo.name) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Transfer player ${event.player.username} to limbo server")
                // how does a play is not authorized when they joined a server? send them to limbo!
                event.player.createConnectionRequest(BloraPlugin.limboServer).fireAndForget()
                return@buildTask
            }

            // now database fetched player is always nonnull
            val databasePlayerByName = BloraPlugin.database.getPlayerByName(event.player.username)

            // show eula to the player if they haven't accepted the eula yet
            if (!databasePlayerByName!!.eulaAccepted) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} hasn't accepted EULA, sending EULA first")
                AuthorizationFunctions.showEulaDialog(event.player)
            } else {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} accepted EULA, sending them login or register dialog")
                AuthorizationFunctions.autoShowLoginDialog(event.player)
            }
        }
            .delay(1.seconds.toJavaDuration())
            .schedule()
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

        if (event.player.isOnlineMode && premiumPlayer != null
            && if (databasePlayerByName != null)
                databasePlayerByName.hashedPassword1 == "%unregistered%"
            else
                true) {
            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s is under online mode")
            /*if (premiumPlayer == null) { // why could this happen?
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s premium data is null")
                event.player.disconnect {
                    localization(event.player) { this.kickLoginError_profiling }
                }
                return
            }*/

            if (databasePlayerByName != null) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by name exists")
                if (databasePlayerByName.premiumUuid == null || databasePlayerByPremiumUuid == null) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by name has no premium uuid")
                    event.player.disconnect {
                        localization(event.player) { this.kickLoginSame_name_offline_player }
                    }
                    return
                }

                if (
                    databasePlayerByPremiumUuid.username != databasePlayerByName.username ||
                    databasePlayerByName.premiumUuid != premiumPlayer.uuid
                ) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip) seems to swap username with another player")
                    // this will happen when two of premium player swap username
                    event.player.disconnect {
                        localization(event.player) { this.kickLoginSwap_premium_username }
                    }
                    return
                }

                BloraPlugin.database.trans {
                    databasePlayerByName.lastJoin = LocalDateTime.now()
                    databasePlayerByName.lastIp = ip
                    databasePlayerByName.flush()
                }
            } else if (databasePlayerByPremiumUuid != null) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by premium uuid exists")
                // premium player changed his/her username
                BloraPlugin.database.trans {
                    databasePlayerByPremiumUuid.username = username.lowercase()
                    databasePlayerByPremiumUuid.lastJoin = LocalDateTime.now()
                    databasePlayerByPremiumUuid.lastIp = ip
                    databasePlayerByPremiumUuid.flush()
                }
            } else {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip) is a newly joined premium player")
                // newly joined premium player
                BloraPlugin.database.trans {
                    databasePlayerByName = PlayerDao.new {
                        this.uuid = uuid
                        this.premiumUuid = premiumPlayer.uuid
                        this.username = username.lowercase()
                        this.hashedPassword1 = "%unregistered%"
                        this.hashedPassword2 = "%unregistered%"
                        this.hashedPassword3 = "%unregistered%"
                        this.firstJoin = LocalDateTime.now()
                        this.lastJoin = LocalDateTime.now()
                        this.firstIp = ip
                        this.lastIp = ip
                        this.lastServer = BloraPlugin.configuration.server.lobby
                        this.email = null
                        this.autoLogin = false
                        this.eulaAccepted = false
                        this.jsonOptions = PlayerOptions(
                            alwaysLobby = OptionStatus.NOT_SET
                        )
                    }

                    databasePlayerByName.flush()
                }
            }
        } else {
            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s is under offline mode")
            if (BloraPlugin.configuration.authorization.onlineFeatures && premiumPlayer != null) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by premium uuid exists, but he's cracked player, kick")
                event.player.disconnect {
                    localization(event.player) { this.kickLoginOnline_profile_but_offline_join }
                }
                return
            } else if (databasePlayerByName == null) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by name not exists")
                // newly joined crack player
                BloraPlugin.database.trans {
                    databasePlayerByName = PlayerDao.new {
                        this.uuid = uuid
                        this.premiumUuid = null
                        this.username = username.lowercase()
                        this.hashedPassword1 = "%unregistered%"
                        this.hashedPassword2 = "%unregistered%"
                        this.hashedPassword3 = "%unregistered%"
                        this.firstJoin = LocalDateTime.now()
                        this.lastJoin = LocalDateTime.now()
                        this.firstIp = ip
                        this.lastIp = ip
                        this.lastServer = BloraPlugin.configuration.server.lobby
                        this.email = null
                        this.autoLogin = false
                        this.eulaAccepted = false
                        this.jsonOptions = PlayerOptions(
                            alwaysLobby = OptionStatus.NOT_SET
                        )
                    }

                    databasePlayerByName.flush()
                }
            } else {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data exists")
                BloraPlugin.database.trans {
                    databasePlayerByName.lastJoin = LocalDateTime.now()
                    databasePlayerByName.lastIp = ip
                    databasePlayerByName.flush()
                }
            }
        }

        BloraPlugin.log.info("[LOGIN SYSTEM] Finishing data updating for player ${event.player.username}($ip)")

        // Warm spy cache from persisted player_options (default off if unset).
        PrivateMessageSpy.load(event.player)

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
        val ip = event.connection.remoteAddress.address.hostAddress

        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s profile is under updating")

        val original = event.gameProfile
        val premiumPlayer = this.premiumData[original.name.lowercase()]

        val uuid =
            if (BloraPlugin.configuration.authorization.uuidGenerator == UUIDGenerator.MOJANG && premiumPlayer != null) {
                premiumPlayer.uuid
            } else {
                UUID.nameUUIDFromBytes(("OfflinePlayer:${original.name.lowercase()}").toByteArray(Charsets.UTF_8))
            }

        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s updated uuid is $uuid")

        event.setGameProfile(GameProfile(uuid, original.name, original.properties))
    }

    // step 1
    @Subscribe(priority = -1000) // DO LAST
    fun onPreLogin(event: PreLoginEvent) {
        if (!event.result.isAllowed)
            return

        val ip = event.connection.remoteAddress.address.hostAddress
        val username = event.username.lowercase()
        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) trying to join proxy server")

        if (BloraPlugin.configuration.authorization.checkUsername) {
            if (!event.connection.isActive) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                return
            }
            BloraPlugin.log.info("[LOGIN SYSTEM] Username validation check is enabled")
            // check username valid or not

            // check min length
            if (username.length < BloraPlugin.configuration.authorization.minUsernameLength) {
                if (!event.connection.isActive) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                    return
                }
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s name length is too short")
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    component {
                        localization(
                            tags = {
                                parsedPlaceholder(
                                    "length",
                                    BloraPlugin.configuration.authorization.minUsernameLength.toString()
                                )
                            }
                        ) {
                            this.kickLoginUsername_too_short
                        }
                    }
                )
                return
            }
            // check max length
            if (username.length > BloraPlugin.configuration.authorization.maxUsernameLength) {
                if (!event.connection.isActive) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                    return
                }
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s name length is too long")
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    component {
                        localization(
                            tags = {
                                parsedPlaceholder(
                                    "length",
                                    BloraPlugin.configuration.authorization.maxUsernameLength.toString()
                                )
                            }
                        ) {
                            this.kickLoginUsername_too_long
                        }
                    }
                )
                return
            }
            // check character valid or not
            if (!username.matches(BloraPlugin.configuration.authorization.usernameRegex.toRegex())) {
                if (!event.connection.isActive) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                    return
                }
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s name doesn't allowed by server provided regex")
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    component {
                        localization(
                            tags = {
                                parsedPlaceholder("regex", BloraPlugin.configuration.authorization.usernameRegex)
                            }
                        ) {
                            this.kickLoginUsername_contains_invalid_characters
                        }
                    }
                )
                return
            }
        }

        // ip limit
        if (BloraPlugin.configuration.security.ipLimit > 0 && !BloraPlugin.configuration.security.ipLimitDisableLogin) {
            if (!event.connection.isActive) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                return
            }
            BloraPlugin.log.info("[LOGIN SYSTEM] IP limit is enabled")
            if (BloraAuthorization.getPlayersUnderIp(ip).size
                >= BloraPlugin.configuration.security.ipLimit
            ) { // use >= because if add one more player will be larger than the limit
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) is limited to join because amount of players logged in on his ip reached limit")
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    component {
                        localization {
                            this.kickLoginSame_ip_login_overcount
                        }
                    }
                )
                return
            }
        }

        // if online features enabled, to do online features, otherwise consider players as offline players
        if (BloraPlugin.configuration.authorization.onlineFeatures) {
            if (!event.connection.isActive) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                return
            }
            BloraPlugin.log.info("[LOGIN SYSTEM] Online features are enabled")
            val fetchResult = PremiumAuthorizer.fetchUserByName(username)

            if (fetchResult is PremiumFetcher.FetchResult.Exists) { // consider player as online players
                val databasePlayer = BloraPlugin.database.getPlayerByName(username)
                if (databasePlayer != null) {
                    if (!event.connection.isActive) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                        return
                    }
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) data exists, use database data to select player login mode")
                    if (databasePlayer.premiumUuid != null) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) data saying they are online mode")
                        this.premiumData[username] = PremiumPlayer(databasePlayer.premiumUuid!!, username.lowercase())
                        event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                    } else {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) data saying they are offline mode")
                        event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                    }
                    return
                }
                if (!event.connection.isActive) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                    return
                }
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username is premium player username")
                this.premiumData[username] = fetchResult.player

                // get to database player object to prevent online player changing their in game name
                val databasePlayerByName = BloraPlugin.database.getPlayerByName(username)
                val databasePlayerByPremiumUuid = BloraPlugin.database.getPlayerByPremiumUuid(fetchResult.player.uuid)

                if (databasePlayerByName != null && databasePlayerByPremiumUuid != null) {
                    if (!event.connection.isActive) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                        return
                    }
                    // same name and premium uuid all exists
                    if (!databasePlayerByPremiumUuid.username.contentEquals(
                            databasePlayerByName.username,
                            ignoreCase = true
                        )
                    ) {
                        if (!event.connection.isActive) {
                            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                            return
                        }
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username is used by a cracked player, due to the crack player joined before this player changes his account username")
                        // if premium uuid refered player and username refered player is not same
                        // the one situation is, A and B all joined the server before, A is online player and B is offline player
                        // B player's username is not registered on Mojang server (crack), A players' username is registered (premium)
                        // but A player changed his/her username to B's which isn't registered on Mojang server
                        // so the username now refers to a new premium player
                        event.result = PreLoginEvent.PreLoginComponentResult.denied(
                            component {
                                localization {
                                    this.kickLoginSame_name_offline_player
                                }
                            }
                        )
                        return
                    }
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s is forced to use online mode")
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode() // online player
                } else if (databasePlayerByPremiumUuid != null) { // means this premium player changed their ign and no offline player is using this name, which is great, it's easy to migrate!
                    if (!event.connection.isActive) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                        return
                    }
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s checked their username, updating")
                    BloraPlugin.database.trans {
                        databasePlayerByPremiumUuid.username = username
                        databasePlayerByPremiumUuid.flush() // save changes to database
                    }
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                } else if (databasePlayerByName != null) {
                    if (!event.connection.isActive) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                        return
                    }
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username was logged in under offline mode before, for security, force he uses offline mode to login")
                    // this player has registered their username under offline mode, just join the game as offline player
                    // one situation is the player isn't premium player before, but now he is
                    // to prevent account is stole by buying premium account for a cracked username, force them join under offline mode
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                } else {
                    if (!event.connection.isActive) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                        return
                    }
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) can normally login under online mode")
                    // other situations should check if this player is in online mode
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                }
            } else if (fetchResult == PremiumFetcher.FetchResult.NotExists) { // data not fetched, consider player as offline player
                if (!event.connection.isActive) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                    return
                }
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) is a cracked player")
                this.premiumData[username] = null
                val databasePlayer = BloraPlugin.database.getPlayerByName(username)

                if (databasePlayer != null) {
                    if (!event.connection.isActive) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                        return
                    }
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username is stored in database")
                    if (databasePlayer.premiumUuid != null) { // a rarely happen event, but it will, it does exist!
                        if (!event.connection.isActive) {
                            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                            return
                        }
                        BloraPlugin.log.info("[LOGIN SYSTEM] Data stored in database related to player ${event.username}($ip)'s username has premium uuid, kick the player")
                        event.result = PreLoginEvent.PreLoginComponentResult.denied(
                            component {
                                localization {
                                    this.kickLoginSame_old_name_online_player
                                }
                            }
                        )
                        return
                    }
                } else {
                    if (!event.connection.isActive) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                        return
                    }
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username isn't stored in database")
                    // when database player not exists, do ip limit test
                    if (BloraPlugin.configuration.security.ipLimit > 0 && !BloraPlugin.configuration.security.ipLimitDisableRegister) {
                        if (!event.connection.isActive) {
                            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                            return
                        }
                        BloraPlugin.log.info("[LOGIN SYSTEM] IP limit is enabled")
                        val amount =
                            if (BloraPlugin.configuration.security.ipLimitStrategyForRegister == Order.FIRST) {
                                BloraPlugin.database.getFirstIpAmount(ip)
                            } else {
                                BloraPlugin.database.getLastIpAmount(ip)
                            }
                        if (amount >= BloraPlugin.configuration.security.ipLimit) { // use >= because if add one more player will be larger than the limit
                            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s ip is reaching the limit")
                            event.result = PreLoginEvent.PreLoginComponentResult.denied(
                                component {
                                    localization {
                                        this.kickLoginSame_ip_register_overcount
                                    }
                                }
                            )
                            return
                        }
                    }
                }

                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) can normally login under offline mode")
                event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode() // offline player
            } else {
                if (!event.connection.isActive) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                    return
                }
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    component {
                        localization {
                            this.kickLoginError_profiling
                        }
                    }
                )
            }
        } else {
            if (!event.connection.isActive) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) login process interrupted because the connection is not active")
                return
            }
            BloraPlugin.log.info("[LOGIN SYSTEM] Online features are disabled")
            event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
        }
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        BloraAuthorization.clear(event.player)
        PrivateMessageSpy.clear(event.player)
        this.passedLoginStatus.remove(event.player)
        this.premiumData.remove(event.player.username.lowercase())
    }

}