package blora.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

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