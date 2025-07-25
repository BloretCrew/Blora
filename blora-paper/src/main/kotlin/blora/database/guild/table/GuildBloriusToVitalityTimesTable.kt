package blora.database.guild.table

import org.jetbrains.exposed.dao.id.IntIdTable

object GuildBloriusToVitalityTimesTable : IntIdTable("blora_guild_blorius_to_vitality_times") {

    val gid = text("gid")
    val times = integer("times")

}