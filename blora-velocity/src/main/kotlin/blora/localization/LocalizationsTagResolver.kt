package blora.localization

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.parsedPlaceholder

class LocalizationsTagResolver(
    private val customPlaceholders: Map<String, String>,
    private val customColors: Map<String, String>,
) : TagResolver {

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context
    ): Tag? {
        if (name != "blora")
            return null

        val type = arguments.popOr("type key expected").value()
        val value = arguments.popOr("value key expected").value()

        return if (type == "color") {
            if (value.length == 7) {
                Tag.styling {
                    it.color(TextColor.fromHexString(value))
                }
            } else if (value.replace(" ", "").contains(",") && value.count { it == ',' } == 2) {
                val split = value.split(",")
                Tag.styling {
                    it.color(
                        TextColor.color(
                            split[0].toInt(),
                            split[1].toInt(),
                            split[2].toInt()
                        )
                    )
                }
            } else if (NamedTextColor.NAMES.keys().contains(value.lowercase())) {
                Tag.styling {
                    it.color(NamedTextColor.NAMES.value(value))
                }
            } else {
                null
            }
        } else if (type == "placeholder") {
            Tag.selfClosingInserting {
                component {
                    var content = customPlaceholders.getOrDefault(value, value)
                    // prevent loopback replacement
                    if (content.contains("<blora:placeholder:$value>"))
                        content = content.replace("<blora:placeholder:$value>", "")
                    mini(content) {
                        parsedPlaceholder(this@LocalizationsTagResolver)
                    }
                }
            }
        } else {
            null
        }
    }

    override fun has(name: String): Boolean {
        return name == "blora"
    }

}