package blora.database.mail

import blora.modules.mail.Attachment
import blora.modules.mail.trigger.OnlineBeforeTrigger
import blora.modules.mail.trigger.OnlineInRangeTrigger
import blora.modules.mail.trigger.OnlineInRecentDaysTrigger
import blora.modules.mail.trigger.Trigger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class SystemMailDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<SystemMailDao>(SystemMailTable)

    var identifier: String by SystemMailTable.identifier
    var sender: String? by SystemMailTable.sender
    var title: String by SystemMailTable.title
    var contents: String by SystemMailTable.contents
    var triggers: String by SystemMailTable.triggers
    var attachment: String by SystemMailTable.attachment
    var creator: UUID by SystemMailTable.creator
    var sendingDate: LocalDateTime? by SystemMailTable.sendingDate
    var createdAt: LocalDateTime by SystemMailTable.createdAt
    var actived: Boolean by SystemMailTable.actived

    var parsedAttachment: Attachment
        get() {
            return Attachment.fromJson(this.attachment)
        }
        set(value) {
            this.attachment = value.toJson()
        }

    var parsedTriggers: Set<Trigger>
        get() {
            val triggers: JsonObject = Json { }.decodeFromString(this.triggers)
            if (triggers.isEmpty()) {
                return emptySet()
            }
            val triggerList = mutableSetOf<Trigger>()
            for (key in triggers.keys) {
                when (key) {
                    "online_before" -> {
                        val numbers = (triggers[key] as JsonPrimitive).content.split(",").map { it.toInt() }
                        triggerList.add(
                            OnlineBeforeTrigger(
                                LocalDate.of(
                                    numbers[0],
                                    numbers[1],
                                    numbers[2],
                                )
                            )
                        )
                    }

                    "online_in_recent_days" -> {
                        val numbers = (triggers[key] as JsonPrimitive).content.split(",").map { it.toInt() }
                        triggerList.add(
                            OnlineInRecentDaysTrigger(
                                LocalDate.of(
                                    numbers[0],
                                    numbers[1],
                                    numbers[2],
                                ),
                                numbers[3]
                            )
                        )
                    }

                    "online_in_range" -> {
                        val numbers = (triggers[key] as JsonPrimitive).content.split(",").map { it.toInt() }
                        triggerList.add(
                            OnlineInRangeTrigger(
                                LocalDate.of(
                                    numbers[0],
                                    numbers[1],
                                    numbers[2],
                                ),
                                LocalDate.of(
                                    numbers[3],
                                    numbers[4],
                                    numbers[5],
                                )
                            )
                        )
                    }
                }
            }
            return triggerList.toSet()
        }
        set(value) {
            val jsonObject = JsonObject(
                value.mapNotNull {
                    when (it) {
                        is OnlineBeforeTrigger -> {
                            "online_before" to "${it.date.year},${it.date.monthValue},${it.date.dayOfMonth}"
                        }

                        is OnlineInRangeTrigger -> {
                            "online_in_range" to "${it.from.year},${it.from.monthValue},${it.from.dayOfMonth},${it.to.year},${it.to.monthValue},${it.to.dayOfMonth}"
                        }

                        is OnlineInRecentDaysTrigger -> {
                            "online_in_recent_days" to "${it.anchorDate.year},${it.anchorDate.monthValue},${it.anchorDate.dayOfMonth},${it.days}"
                        }

                        else -> {
                            null
                        }
                    }
                }.map {
                    it.first to JsonPrimitive(it.second)
                }.associate { it }
            )
            this.triggers = Json { }.encodeToString(jsonObject)
        }

}