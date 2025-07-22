package blora.guild

import java.time.LocalDateTime
import java.util.UUID

data class ReviewResult(
    val reviewer: UUID,
    val reviewedAt: LocalDateTime
)
