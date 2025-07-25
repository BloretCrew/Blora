package blora.database.player.dao

import blora.database.player.table.PlayerLoginTable
import blora.database.player.table.PlayerOnlineDataTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate
import java.util.*

class PlayerOnlineDataDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<PlayerOnlineDataDao>(PlayerOnlineDataTable)

    var playerUuid: UUID by PlayerOnlineDataTable.uuid
    var onlineData by PlayerOnlineDataTable.online

}