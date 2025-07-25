package blora.util

import blora.configuration.CONF
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDate as jtLocalDate
import java.time.LocalDateTime as jtLocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

val BASE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun jtLocalDateTime.castString(): String {
    return BASE_DATE_FORMAT.format(Date.from(this.atZone(ZoneId.systemDefault()).toInstant()))
}

fun jtLocalDate.castString(): String {
    return "$year 年 $monthValue 月 $dayOfMonth 日"
}

infix fun LocalDateTime.deltaSeconds(another: LocalDateTime): Long {
    return abs(ChronoUnit.SECONDS.between(this.toJavaLocalDateTime(), another.toJavaLocalDateTime()))
}

infix fun jtLocalDateTime.deltaSeconds(another: jtLocalDateTime): Long {
    return abs(ChronoUnit.SECONDS.between(this, another))
}