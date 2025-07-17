package blora.internal.api.extension

import blora.api.QuickEntityLib
import org.bukkit.entity.Entity

val Entity.eyeHeight: Float
    get() {
        return QuickEntityLib.getEyeHeight(this)
    }