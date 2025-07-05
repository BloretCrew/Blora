package blora.modules.database

import blora.modules.Module
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.style.rgb
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with

object DatabaseModule : Module {

    override val id: String = "database"
    override val name: String = "Database"
    override val displayName: Component = component {
        text("数据库模块") with rgb(137, 129, 124).text
    }
    override var enabled: Boolean = false
    override val dependencies: List<String> = listOf()

}