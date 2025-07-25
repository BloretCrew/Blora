package blora.database.guild.dao

import blora.database.guild.table.GuildBloriusToVitalityTimesTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildBloriusToVitalityTimesDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildBloriusToVitalityTimesDao>(GuildBloriusToVitalityTimesTable)

    var guildId by GuildBloriusToVitalityTimesTable.gid
    var times by GuildBloriusToVitalityTimesTable.times

}