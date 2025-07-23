@file:Suppress("UnstableApiUsage")

package blora.extension

import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Bukkit
import java.util.UUID

fun UUID.playerProfile(): PlayerProfile {
    return Bukkit.getOfflinePlayer(this).playerProfile
}

fun UUID.resolvableProfile(): ResolvableProfile {
    return ResolvableProfile.resolvableProfile(Bukkit.getOfflinePlayer(this).playerProfile)
}