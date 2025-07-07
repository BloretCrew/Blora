package blora.modules.mail

import blora.modules.Module
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.style.rgb
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with

object MailModule : Module {

    override val id: String = "mail"
    override val name: String = "Mail"
    override val displayName: Component = component {
        text("邮件") with rgb(137, 129, 124).text
    }
    override var enabled: Boolean = false
    override val dependencies: List<String> = listOf()

    override fun enable() {

    }

}