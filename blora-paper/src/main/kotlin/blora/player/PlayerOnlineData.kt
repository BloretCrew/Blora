package blora.player

import blora.util.deltaSeconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.Serializable

import java.time.LocalDateTime as jtLocalDateTime

@Serializable
data class PlayerOnlineData(
    val onlineData: List<Pair<LocalDateTime, LocalDateTime>>
) {

    fun hasTimesOfHoursAfter(
        after: jtLocalDateTime,
        hours: Int,
        playerJoinAt: jtLocalDateTime
    ): Pair<jtLocalDateTime, Int> {
        var times = 0
        var tempSeconds = 0L
        var maybeDateTime = after
        for ((fromKt, toKt) in this.onlineData) {
            val from = fromKt.toJavaLocalDateTime()
            val to = toKt.toJavaLocalDateTime()
            if (to < after)
                continue
            val deltaSeconds = to deltaSeconds if (after < from) from else after
            if (tempSeconds + deltaSeconds >= (hours * 60 * 60)) {
                times += 1
                val originalTempSeconds = tempSeconds
                tempSeconds = tempSeconds + deltaSeconds - hours * 60 * 60
                maybeDateTime = (if (after < from) from else after).plusSeconds(
                    hours * 60 * 60 - originalTempSeconds
                )
            } else {
                tempSeconds += deltaSeconds
            }
        }
        val from = playerJoinAt
        val to = jtLocalDateTime.now()
        if (to >= after) {
            val deltaSeconds = to deltaSeconds if (after < from) from else after
            if (tempSeconds + deltaSeconds >= (hours * 60 * 60)) {
                times += 1
                maybeDateTime = (if (after < from) from else after).plusSeconds(
                    hours * 60 * 60 - tempSeconds
                )
            }
        }
        return maybeDateTime to times
    }

    fun hasTimesOfMinutesAfter(
        after: jtLocalDateTime,
        minutes: Int,
        playerJoinAt: jtLocalDateTime
    ): Pair<jtLocalDateTime, Int> {
        var times = 0
        var tempSeconds = 0L
        var maybeDateTime = after
        for ((fromKt, toKt) in this.onlineData) {
            val from = fromKt.toJavaLocalDateTime()
            val to = toKt.toJavaLocalDateTime()
            if (to < after)
                continue
            val deltaSeconds = to deltaSeconds if (after < from) from else after
            if (tempSeconds + deltaSeconds >= (minutes * 60)) {
                times += 1
                val originalTempSeconds = tempSeconds
                tempSeconds = tempSeconds + deltaSeconds - minutes * 60
                maybeDateTime = (if (after < from) from else after).plusSeconds(
                    minutes * 60 - originalTempSeconds
                )
            } else {
                tempSeconds += deltaSeconds
            }
        }
        val from = playerJoinAt
        val to = jtLocalDateTime.now()
        if (to >= after) {
            val deltaSeconds = to deltaSeconds if (after < from) from else after
            if (tempSeconds + deltaSeconds >= (minutes * 60)) {
                times += 1
                maybeDateTime = (if (after < from) from else after).plusSeconds(
                    minutes * 60 - tempSeconds
                )
            }
        }
        return maybeDateTime to times
    }

}