package blora.player

import java.time.LocalDate
import java.util.*

class PlayerLoginData(
    val uuid: UUID,
    private val dates: List<LocalDate>
) {

    fun anyIn(from: LocalDate, to: LocalDate): Boolean {
        return this.dates.any { it in from..to }
    }

    fun anyBefore(date: LocalDate): Boolean {
        return this.dates.any { it <= date }
    }

    fun anyAfter(date: LocalDate): Boolean {
        return this.dates.any { it <= date }
    }

    fun isLoginInDays(anchorDate: LocalDate, days: Long): Boolean {
        if (days <= 0) {
            return false
        }
        return this.dates.any { it in anchorDate.minusDays(days)..anchorDate }
    }

}