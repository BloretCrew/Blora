package net.deechael.blora.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.GameProfileRequestEvent
import com.velocitypowered.api.event.player.configuration.PlayerFinishedConfigurationEvent
import com.velocitypowered.api.util.GameProfile
import io.github._4drian3d.vpacketevents.api.event.PacketReceiveEvent
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
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
import net.deechael.blora.protocol.packet.CustomClickAction
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object BasicListener {

    private val premiumData: MutableMap<String, PremiumPlayer?> = mutableMapOf()

    @Subscribe
    fun onPacketReceive(event: PacketReceiveEvent) {
        val packet = event.packet
        if (packet is CustomClickAction) {
            if (packet.id == "minecraft:blora_eula_accept") {
                AuthDialog.eulaDialogCallback(event.player, true)
            } else if (packet.id == "minecraft:blora_eula_reject") {
                AuthDialog.eulaDialogCallback(event.player, false)
            } else if (packet.id == "minecraft:blora_login") {
                AuthDialog.loginDialogCallback(
                    event.player,
                    ((packet.payload as NbtCompound)["blora_password"] as NbtString).value
                )
            } else if (packet.id == "minecraft:blora_register") {
                AuthDialog.registerDialogCallback(
                    event.player,
                    ((packet.payload as NbtCompound)["blora_password"] as NbtString).value,
                    ((packet.payload as NbtCompound)["blora_confirm_password"] as NbtString).value
                )
            } else if (packet.id == "minecraft:blora_exit") {
                event.player.disconnect(component {
                    mini(BloraPlugin.configuration.messages.ingameKickExit)
                })
            }
        }
    }

    @Subscribe
    fun onFinishedConfiguration(event: PlayerFinishedConfigurationEvent) {
        BloraPlugin.proxyServer.scheduler.buildTask(BloraPlugin.instance) { task ->
            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} is connecting to server ${event.server.serverInfo.name}")
            // notice: when testing, comment the check below to avoid cannot do test
            val databasePlayer = BloraPlugin.database.getPlayerByName(event.player.username)
            if (event.server.serverInfo.name == BloraPlugin.limboServer.serverInfo.name) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} is in limbo server")
                if (!databasePlayer!!.eulaAccepted) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} hasn't accepted EULA, sending EULA")
                    AuthDialog.showEulaDialog(event.player)
                } else {
                    if (BloraAuthorization.isAuthorized(event.player)) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} is authorized, stay in their server")
                        if (!databasePlayer.eulaAccepted) {
                            AuthDialog.showEulaDialog(event.player)
                        }
                        return@buildTask
                    }

                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} accepted EULA, sending them login or register dialog")
                    AuthDialog.autoShowLoginDialog(event.player)
                }
                return@buildTask
            }

            if (BloraAuthorization.isAuthorized(event.player)) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} is authorized, stay in their server")
                if (!databasePlayer!!.eulaAccepted) {
                    AuthDialog.showEulaDialog(event.player)
                }
                return@buildTask
            }

            BloraPlugin.log.info("[LOGIN SYSTEM] Transfer player ${event.player.username} to limbo server")
            // how does a play is not authorized when they joined a server? send them to limbo!
            event.player.createConnectionRequest(BloraPlugin.limboServer).fireAndForget()

            // now database fetched player is always nonnull
            val databasePlayerByName = BloraPlugin.database.getPlayerByName(event.player.username)

            // show eula to the player if they haven't accepted the eula yet
            if (!databasePlayerByName!!.eulaAccepted) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} hasn't accepted EULA, sending EULA first")
                AuthDialog.showEulaDialog(event.player)
            } else {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username} accepted EULA, sending them login or register dialog")
                AuthDialog.autoShowLoginDialog(event.player)
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

        if (event.player.isOnlineMode) {
            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s is under online mode")
            if (premiumPlayer == null) { // why could this happen?
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s premium data is null")
                event.player.disconnect(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickErrorProfiling
                    )
                )
                return
            }

            if (databasePlayerByName != null) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by name exists")
                if (databasePlayerByName.premiumUuid == null || databasePlayerByPremiumUuid == null) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by name has no premium uuid")
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
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip) seems to swap username with another player")
                    // this will happen when two of premium player swap username
                    event.player.disconnect(
                        BloraConstants.Objects.miniMessage.deserialize(
                            BloraPlugin.configuration.messages.loginKickSwapPremiumUsername
                        )
                    )
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
                    databasePlayerByName = BloraPlayer.new {
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
                    }

                    databasePlayerByName.flush()
                }
            }
        } else {
            BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s is under offline mode")
            if (premiumPlayer != null) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by premium uuid exists, but he's cracked player, kick")
                event.player.disconnect(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickOnlineProfileButOfflineJoin
                    )
                )
                return
            } else if (databasePlayerByName == null) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.player.username}($ip)'s database data queried by name not exists")
                // newly joined crack player
                BloraPlugin.database.trans {
                    databasePlayerByName = BloraPlayer.new {
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

            // send player to limbo server and wait for logging in, only crack player needs this
            event.player.createConnectionRequest(BloraPlugin.limboServer).fireAndForget()
        }

        BloraPlugin.log.info("[LOGIN SYSTEM] Finishing data updating for player ${event.player.username}($ip)")

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
            BloraPlugin.log.info("[LOGIN SYSTEM] Username validation check is enabled")
            // check username valid or not

            // check min length
            if (username.length < BloraPlugin.configuration.authorization.minUsernameLength) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s name length is too short")
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickUsernameTooShort,
                        Placeholder.parsed(
                            "length",
                            BloraPlugin.configuration.authorization.minUsernameLength.toString()
                        )
                    )
                )
                return
            }
            // check max length
            if (username.length > BloraPlugin.configuration.authorization.maxUsernameLength) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s name length is too long")
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    BloraConstants.Objects.miniMessage.deserialize(
                        BloraPlugin.configuration.messages.loginKickUsernameTooLong,
                        Placeholder.parsed(
                            "length",
                            BloraPlugin.configuration.authorization.maxUsernameLength.toString()
                        )
                    )
                )
                return
            }
            // check character valid or not
            if (!username.matches(BloraPlugin.configuration.authorization.usernameRegex.toRegex())) {
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s name doesn't allowed by server provided regex")
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
            BloraPlugin.log.info("[LOGIN SYSTEM] IP limit is enabled")
            if (BloraAuthorization.getPlayersUnderIp(ip).size
                >= BloraPlugin.configuration.security.ipLimit
            ) { // use >= because if add one more player will be larger than the limit
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) is limited to join because amount of players logged in on his ip reached limit")
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
            BloraPlugin.log.info("[LOGIN SYSTEM] Online features are enabled")
            val fetchResult = PremiumAuthorizer.fetchUserByName(username)

            if (fetchResult is PremiumFetcher.FetchResult.Exists) { // consider player as online players
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username is premium player username")
                this.premiumData[username] = fetchResult.player

                // get to database player object to prevent online player changing their in game name
                val databasePlayerByName = BloraPlugin.database.getPlayerByName(username)
                val databasePlayerByPremiumUuid = BloraPlugin.database.getPlayerByPremiumUuid(fetchResult.player.uuid)

                if (databasePlayerByName != null && databasePlayerByPremiumUuid != null) {
                    // same name and premium uuid all exists
                    if (!databasePlayerByPremiumUuid.username.contentEquals(
                            databasePlayerByName.username,
                            ignoreCase = true
                        )
                    ) {
                        BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username is used by a cracked player, due to the crack player joined before this player changes his account username")
                        // if premium uuid refered player and username refered player is not same
                        // the one situation is, A and B all joined the server before, A is online player and B is offline player
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
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s is forced to use online mode")
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode() // online player
                } else if (databasePlayerByPremiumUuid != null) { // means this premium player changed their ign and no offline player is using this name, which is great, it's easy to migrate!
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s checked their username, updating")
                    BloraPlugin.database.trans {
                        databasePlayerByPremiumUuid.username = username
                        databasePlayerByPremiumUuid.flush() // save changes to database
                    }
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                } else if (databasePlayerByName != null) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username was logged in under offline mode before, for security, force he uses offline mode to login")
                    // this player has registered their username under offline mode, just join the game as offline player
                    // one situation is the player isn't premium player before, but now he is
                    // to prevent account is stole by buying premium account for a cracked username, force them join under offline mode
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                } else {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) can normally login under online mode")
                    // other situations should check if this player is in online mode
                    event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                }
            } else { // data not fetched, consider player as offline player
                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) is a cracked player")
                this.premiumData[username] = null
                val databasePlayer = BloraPlugin.database.getPlayerByName(username)

                if (databasePlayer != null) {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username is stored in database")
                    if (databasePlayer.premiumUuid != null) { // a rarely happen event, but it will, it does exist!
                        BloraPlugin.log.info("[LOGIN SYSTEM] Data stored in database related to player ${event.username}($ip)'s username has premium uuid, kick the player")
                        event.result = PreLoginEvent.PreLoginComponentResult.denied(
                            BloraConstants.Objects.miniMessage.deserialize(
                                BloraPlugin.configuration.messages.loginKickSameOldNameOnlinePlayer
                            )
                        )
                        return
                    }
                } else {
                    BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip)'s username isn't stored in database")
                    // when database player not exists, do ip limit test
                    if (BloraPlugin.configuration.security.ipLimit > 0 && !BloraPlugin.configuration.security.ipLimitDisableRegister) {
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
                                BloraConstants.Objects.miniMessage.deserialize(
                                    BloraPlugin.configuration.messages.loginKickSameIpRegisterOvercount
                                )
                            )
                            return
                        }
                    }
                }

                BloraPlugin.log.info("[LOGIN SYSTEM] Player ${event.username}($ip) can normally login under offline mode")
                event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode() // offline player
            }
        } else {
            BloraPlugin.log.info("[LOGIN SYSTEM] Online features are disabled")
            event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
        }
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        BloraAuthorization.clear(event.player)
        this.premiumData.remove(event.player.username.lowercase())
    }

}