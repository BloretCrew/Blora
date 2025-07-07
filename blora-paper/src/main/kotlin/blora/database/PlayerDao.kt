package blora.database

import blora.options.PLAYER_OPTIONS_JSON
import blora.options.PlayerOptions
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime
import java.util.*

class PlayerDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<PlayerDao>(PlayerTable)

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