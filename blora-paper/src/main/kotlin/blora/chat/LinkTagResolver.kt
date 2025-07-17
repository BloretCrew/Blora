package blora.chat

import blora.extension.localization
import blora.plugin.BloraPlugin
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.style.openUrl
import plutoproject.adventurekt.text.unparsedPlaceholder
import plutoproject.adventurekt.text.with

object LinkTagResolver : TagResolver {

    private val URL_REGEX = "^(https?://)?([\\w-]+\\.)+[\\w-]+(:\\d+)?(/[\\w\\-.~!*'();:@&=+$,?#/]*)?$".toRegex()

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context
    ): Tag? {
        if (name != "link")
            return null
        if (!arguments.hasNext())
            return null
        val url = arguments.pop().value()
            .let {
                var builder = StringBuilder(it)
                while (arguments.hasNext())
                    builder = builder.append(":").append(arguments.pop())
                return@let builder.toString()
            }
        if (!url.matches(URL_REGEX))
            return null
        val pointered = ctx.target()
        val player: Player? = pointered as? Player
        return Tag.selfClosingInserting {
            component {
                localization(
                    player = player,
                    tags = {
                        unparsedPlaceholder("link", url)
                    }
                ) {
                    BloraPlugin.configuration.chat.linkPlaceholderFormat
                } with openUrl(url)
            }
        }
    }

    override fun has(name: String): Boolean {
        return name == "link"
    }

}