package blora.database.player.dao

import blora.database.player.table.PlayerInfoTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class PlayerInfoDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<PlayerInfoDao>(PlayerInfoTable)

    var playerUuid: UUID by PlayerInfoTable.uuid
    var username: String by PlayerInfoTable.name

}