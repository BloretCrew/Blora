package blora.localization

import org.bukkit.entity.Player

fun i18n(player: Player? = null, fetcher: LocalizationContents.() -> String): String {
    return if (player != null) {
        BloraLocalization.getLocalization(player)
    } else {
        BloraLocalization.fallbackLocalization
    }.fetcher()
}