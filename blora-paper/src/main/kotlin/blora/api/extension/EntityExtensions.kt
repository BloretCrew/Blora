package blora.api.extension

import org.bukkit.entity.Entity
import blora.api.QuickEntityLib

val Entity.eyeHeight: Float
    get() {
        return QuickEntityLib.getEyeHeight(this)
    }