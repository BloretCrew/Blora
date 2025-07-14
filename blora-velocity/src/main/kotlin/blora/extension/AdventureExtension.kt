package blora.extension

import blora.localization.BloraLocalization
import blora.localization.LocalizationContents
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.text.ComponentKt
import plutoproject.adventurekt.text.MiniMessageContext
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.parsedPlaceholder

fun ComponentKt.localization(
    player: Player? = null,
    tags: MiniMessageContext.() -> Unit = {},
    content: LocalizationContents.() -> String
): ComponentKt {
    return if (player != null) {
        val localization = BloraLocalization.getLocalization(player)
        this.mini(content(localization)) {
            tags()
            parsedPlaceholder(localization.resolver())
        }
    } else {
        val localization = BloraLocalization.fallbackLocalization
        this.mini(content(localization)) {
            tags()
            parsedPlaceholder(localization.resolver())
        }
    }
}