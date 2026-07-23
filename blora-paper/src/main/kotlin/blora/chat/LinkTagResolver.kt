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
import java.net.URLDecoder

object LinkTagResolver : TagResolver {

    val URL_REGEX = "^(https?://)?([\\w-]+\\.)+[\\w-]+(:\\d+)?(/[\\w\\-.~!*'();:@%&=+$,?#/]*)?$".toRegex()

    fun parseValidUrl(argument: String): String? {
        val url = argument.removeMatchingQuotes()
        return url.takeIf { it.matches(URL_REGEX) }
    }

    fun safeDecode(url: String): String {
        return try {
            URLDecoder.decode(url, "UTF-8")
        } catch (_: IllegalArgumentException) {
            url
        }
    }

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
            .let(::parseValidUrl)
            ?: return null
        val pointered = ctx.target()
        val player: Player? = pointered as? Player
        return Tag.selfClosingInserting {
            component {
                localization(
                    player = player,
                    tags = {
                        unparsedPlaceholder("link", safeDecode(url))
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

    private fun String.removeMatchingQuotes(): String {
        return if (length >= 2 && ((startsWith('"') && endsWith('"')) || (startsWith('\'') && endsWith('\'')))) {
            substring(1, length - 1)
        } else {
            this
        }
    }

}