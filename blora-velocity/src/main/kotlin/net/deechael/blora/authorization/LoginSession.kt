package net.deechael.blora.authorization

import java.util.*

data class LoginSession(
    val uuid: UUID,
    val ip: String,
    val quitTime: Long
)
