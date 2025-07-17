@file:OptIn(ExperimentalTime::class)

package blora.extension

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

fun Instant.Companion.sysNow(): java.time.Instant {
    return Clock.System.now().toJavaInstant()
}