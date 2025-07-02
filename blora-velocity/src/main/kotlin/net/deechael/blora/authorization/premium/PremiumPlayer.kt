package net.deechael.blora.authorization.premium

import java.util.UUID

data class PremiumPlayer(
    val uuid: UUID,
    val username: String, // lowercase
)
