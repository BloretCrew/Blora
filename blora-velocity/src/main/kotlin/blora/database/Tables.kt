package blora.database

import blora.options.PLAYER_OPTIONS_JSON
import blora.options.PlayerOptions
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*

object PlayerTable : IntIdTable("blora_players") {

    val uuid = uuid("uuid")
    val premiumUuid = uuid("premium_uuid").nullable()
    val username = text("username")
    val hashedPassword1 = text("hashed_password_1")
    val hashedPassword2 = text("hashed_password_2")
    val hashedPassword3 = text("hashed_password_3")
    val firstJoin = datetime("first_join")
    val lastJoin = datetime("last_join")
    val firstIp = text("first_ip")
    val lastIp = text("last_ip")
    val lastServer = text("last_server")
    val email = text("email").nullable()
    val autoLogin = bool("auto_login")
    val eulaAccepted = bool("eula_accepted")
    val playerOptions = text("player_options")

}

class BloraPlayer(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<BloraPlayer>(PlayerTable)

    var uuid: UUID by PlayerTable.uuid
    var premiumUuid: UUID? by PlayerTable.premiumUuid
    var username: String by PlayerTable.username
    var hashedPassword1: String by PlayerTable.hashedPassword1
    var hashedPassword2: String by PlayerTable.hashedPassword2
    var hashedPassword3: String by PlayerTable.hashedPassword3
    var firstJoin: LocalDateTime by PlayerTable.firstJoin
    var lastJoin: LocalDateTime by PlayerTable.lastJoin
    var firstIp: String by PlayerTable.firstIp
    var lastIp: String by PlayerTable.lastIp
    var lastServer: String by PlayerTable.lastServer
    var email: String? by PlayerTable.email
    var autoLogin: Boolean by PlayerTable.autoLogin
    var eulaAccepted: Boolean by PlayerTable.eulaAccepted
    var playerOptions: String by PlayerTable.playerOptions
    var jsonOptions: PlayerOptions
        get() = PLAYER_OPTIONS_JSON.decodeFromString(playerOptions)
        set(value) {
            playerOptions = PLAYER_OPTIONS_JSON.encodeToString(value)
        }

    fun hashedPassword(): Triple<String, String, String> {
        return Triple(hashedPassword1, hashedPassword2, hashedPassword3)
    }

}