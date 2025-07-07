package blora.modules.mail

import net.kyori.adventure.text.Component
import java.util.*

data class Mail(
    val receiver: UUID,
    val sender: Sender,
    val title: Component,
    val content: Component,
)