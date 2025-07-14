package blora.database.redeem

import org.jetbrains.exposed.dao.id.IntIdTable

object PlayerRedeemTable : IntIdTable("blora_player_used_redeems") {

    val player = uuid("player")
    val code = text("code")

}