package blora.extension

import blora.localization.BloraLocalization
import blora.localization.LocalizationContents
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.text.ComponentKt
import plutoproject.adventurekt.text.MiniMessageContext
import plutoproject.adventurekt.text.mini

fun ComponentKt.localization(tags: MiniMessageContext.() -> Unit = {}, content: LocalizationContents.() -> String) {
    this.mini(content(BloraLocalization.fallbackLocalization), tags)
}

fun ComponentKt.localization(
    player: Player,
    tags: MiniMessageContext.() -> Unit = {},
    content: LocalizationContents.() -> String
) {
    this.mini(content(BloraLocalization.getLocalization(player)), tags)
}