package blora.database.player

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate
import java.util.*

class PlayerLoginDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<PlayerLoginDao>(PlayerLoginTable)

    var playerUuid: UUID by PlayerLoginTable.uuid
    var date: LocalDate by PlayerLoginTable.date

}