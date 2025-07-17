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

object CopyTagResolver : TagResolver {

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context
    ): Tag? {
        if (name != "copy")
            return null
        if (!arguments.hasNext())
            return null
        val copyText = arguments.pop().value()
            .let {
                var builder = StringBuilder(it)
                while (arguments.hasNext())
                    builder = builder.append(":").append(arguments.pop())
                return@let builder.toString()
            }
        val pointered = ctx.target()
        val player: Player? = pointered as? Player
        return Tag.selfClosingInserting {
            component {
                localization(
                    player = player,
                    tags = {
                        unparsedPlaceholder("text", copyText)
                    }
                ) {
                    BloraPlugin.configuration.chat.copyPlaceholderFormat
                } with suggestCommand(copyText)
            }
        }
    }

    override fun has(name: String): Boolean {
        return name == "copy"
    }

}