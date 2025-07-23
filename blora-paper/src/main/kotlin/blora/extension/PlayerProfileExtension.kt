@file:Suppress("UnstableApiUsage")

package blora.extension

import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.datacomponent.item.ResolvableProfile

fun PlayerProfile.resolvable(): ResolvableProfile {
    return ResolvableProfile.resolvableProfile(this)
}