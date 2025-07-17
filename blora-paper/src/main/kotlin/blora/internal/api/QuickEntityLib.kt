package blora.internal.api

import org.bukkit.entity.Entity

interface QuickEntityLib {

    fun getEyeHeight(entity: Entity): Float

    companion object : QuickEntityLib by QuickLib.getEntityLib()

}