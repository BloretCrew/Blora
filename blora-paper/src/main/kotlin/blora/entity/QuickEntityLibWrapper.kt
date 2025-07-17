package blora.entity

import blora.internal.api.QuickEntityLib
import blora.nms.nms
import org.bukkit.entity.Entity

object QuickEntityLibWrapper : QuickEntityLib {

    override fun getEyeHeight(entity: Entity): Float {
        return entity.nms().eyeHeight
    }

}