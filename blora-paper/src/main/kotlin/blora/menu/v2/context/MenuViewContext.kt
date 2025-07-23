package blora.menu.v2.context

import blora.menu.v2.Menu
import org.bukkit.entity.Player

interface MenuViewContext {

    val menu: Menu
    val viewer: Player
        get() = this.menu.viewer

}