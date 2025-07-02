package net.deechael.blora.authorization

import java.util.UUID

data class LoginSession(
    val uuid: UUID,
    val ip: String,
    val quitTime: Long
)
