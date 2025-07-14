package blora.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

val BASE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun LocalDateTime.castString(): String {
    return BASE_DATE_FORMAT.format(Date.from(this.atZone(ZoneId.systemDefault()).toInstant()))
}

fun LocalDate.castString(): String {
    return "$year 年 $monthValue 月 $dayOfMonth 日"
}