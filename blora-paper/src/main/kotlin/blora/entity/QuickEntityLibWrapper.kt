package blora.entity

import org.bukkit.entity.Entity
import blora.api.QuickEntityLib
import blora.nms.nms

object QuickEntityLibWrapper : QuickEntityLib {

    override fun getEyeHeight(entity: Entity): Float {
        return entity.nms().eyeHeight
    }

}