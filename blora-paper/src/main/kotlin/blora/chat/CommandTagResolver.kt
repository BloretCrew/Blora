package blora.chat

import blora.extension.localization
import blora.plugin.BloraPlugin
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.style.suggestCommand
import plutoproject.adventurekt.text.unparsedPlaceholder
import plutoproject.adventurekt.text.with

object CommandTagResolver : TagResolver {

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context
    ): Tag? {
        if (name != "cmd")
            return null
        if (!arguments.hasNext())
            return null
        val command = arguments.pop().value()
            .let {
                var builder = StringBuilder(it)
                while (arguments.hasNext())
                    builder = builder.append(":").append(arguments.pop())
                return@let builder.toString()
            }
        if (!command.startsWith("/"))
            return null
        val pointered = ctx.target()
        val player: Player? = pointered as? Player
        return Tag.selfClosingInserting {
            component {
                localization(
                    player = player,
                    tags = {
                        unparsedPlaceholder("command", command)
                    }
                ) {
                    BloraPlugin.configuration.chat.commandPlaceholderFormat
                } with suggestCommand(command)
            }
        }
    }

    override fun has(name: String): Boolean {
        return name == "cmd"
    }

}