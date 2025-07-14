package blora.database.redeem

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PlayerRedeemDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<PlayerRedeemDao>(PlayerRedeemTable)

    var player by PlayerRedeemTable.player
    var code by PlayerRedeemTable.code

}