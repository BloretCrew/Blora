package net.deechael.blora

import net.kyori.adventure.text.minimessage.MiniMessage

object BloraConstants {

    object Regexs {
        val USERNAME: Regex = "[a-zA-Z0-9_]*".toRegex()
    }

    object Objects {
        val miniMessage: MiniMessage = MiniMessage.miniMessage()
    }

}