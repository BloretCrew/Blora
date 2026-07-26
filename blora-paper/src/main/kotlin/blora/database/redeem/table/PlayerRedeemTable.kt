package blora.database.redeem.table

import org.jetbrains.exposed.dao.id.IntIdTable

object PlayerRedeemTable : IntIdTable("blora_player_used_redeems") {

    val player = uuid("player")
    val code = text("code")

    init {
        // Prevents double-redeem if application locks regress.
        uniqueIndex(player, code)
    }

}