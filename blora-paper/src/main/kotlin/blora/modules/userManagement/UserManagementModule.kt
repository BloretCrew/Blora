package blora.modules.userManagement

import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.style.rgb
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with
import blora.modules.Module

object UserManagementModule : Module {

    override val id: String = "user_management"
    override val name: String = "UserManagement"
    override val displayName: Component = component {
        text("用户管理模块") with rgb(137, 129, 124).text
    }
    override var enabled: Boolean = false
    override val dependencies: List<String> = listOf(
        "database"
    )

    override fun enable() {

    }


}