package blora.chat

import blora.adventure.PlaceholderAPITagResolver
import blora.extension.asDisplayName
import blora.extension.localization
import blora.plugin.BloraPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.*
import plutoproject.adventurekt.text.style.*
import java.net.URLDecoder

object Chatting {

    fun miniMessageSupport(
        sender: Player,
        viewer: Player,
        rawMessage: String,
        itemInMainHand: ItemStack = sender.inventory.itemInMainHand.clone(),
        replacements: ComponentReplacements.() -> Unit
    ): Component = component {
        replacements(replacements)
        localization(
            player = viewer,
            tags = {
                if (rawMessage.contains("<item>")) { // only add placeholder when need to reduce memory usage
                    if (!itemInMainHand.type.isAir) {
                        componentPlaceholder("item") {
                            localization(
                                player = viewer,
                                tags = {
                                    componentPlaceholder("item") {
                                        raw { itemInMainHand.asDisplayName() }
                                    }
                                    parsedPlaceholder(PlaceholderAPITagResolver(sender))
                                },
                                papi = false
                            ) {
                                BloraPlugin.configuration.chat.itemPlaceholderFormat
                            } with object : WithStyle {
                                override fun with(
                                    holder: ComponentKt,
                                    original: Component
                                ): Component {
                                    return original.hoverEvent(itemInMainHand.asHoverEvent())
                                }
                            } with callback(
                                ClickCallback.Options.builder()
                                    .uses(-1)
                                    .build()
                            ) {
                                if (it != viewer)
                                    return@callback
                                PlayerItemView.view(viewer, itemInMainHand)
                            }
                        }
                    }
                }
                if (rawMessage.contains("<inv>")) {
                    componentPlaceholder("inv") {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("player", sender.name)
                                parsedPlaceholder(PlaceholderAPITagResolver(sender))
                            },
                            papi = false
                        ) {
                            BloraPlugin.configuration.chat.inventoryPlaceholderFormat
                        } with callback(
                            ClickCallback.Options.builder()
                                .uses(-1)
                                .build()
                        ) {
                            if (it != viewer)
                                return@callback
                            PlayerInventoryView.view(viewer, sender)
                        } with showText {
                            localization(viewer) {
                                this.chatViewInventoryTooltip
                            }
                        }
                    }
                }
                if (rawMessage.contains("<enderchest>")) {
                    componentPlaceholder("enderchest") {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("player", sender.name)
                                parsedPlaceholder(PlaceholderAPITagResolver(sender))
                            },
                            papi = false
                        ) {
                            BloraPlugin.configuration.chat.enderChestPlaceholderFormat
                        } with callback(
                            ClickCallback.Options.builder()
                                .uses(-1)
                                .build()
                        ) {
                            if (it != viewer)
                                return@callback
                            PlayerInventoryView.viewEnderChest(viewer, sender)
                        } with showText {
                            localization(viewer) {
                                this.chatViewEnderChestTooltip
                            }
                        }
                    }
                }
                parsedPlaceholder(CommandTagResolver)
                parsedPlaceholder(CopyTagResolver)
                parsedPlaceholder(LinkTagResolver)
                for ((key, value) in BloraPlugin.configuration.chat.placeholders) {
                    if (rawMessage.contains("<$key>")) // for custom tags, do not parse them if not exists
                        parsedPlaceholder(key, value)
                }
                parsedPlaceholder(PlaceholderAPITagResolver(sender))
            },
            papi = false
        ) {
            rawMessage
        }
    }

    fun noMiniMessageSupport(
        sender: Player,
        viewer: Player,
        rawMessage: String,
        itemInMainHand: ItemStack = sender.inventory.itemInMainHand.clone(),
        replacements: ComponentReplacements.() -> Unit
    ): Component = component {
        replacements {
            replacements()
            replacement {
                match("<((?:[^'\"<>]|'[^']*'|\"[^\"]*\")*)>")
                replacement { matchResult, builder ->
                    val content = matchResult.group(1)

                    if (content == "item" && !itemInMainHand.isEmpty) {
                        component {
                            localization(
                                player = viewer,
                                tags = {
                                    componentPlaceholder("item") {
                                        raw { itemInMainHand.asDisplayName() }
                                    }
                                    parsedPlaceholder(PlaceholderAPITagResolver(sender))
                                },
                                papi = false
                            ) {
                                BloraPlugin.configuration.chat.itemPlaceholderFormat
                            } with object : WithStyle {
                                override fun with(
                                    holder: ComponentKt,
                                    original: Component
                                ): Component {
                                    return original.hoverEvent(itemInMainHand.asHoverEvent())
                                }
                            } with callback(
                                ClickCallback.Options.builder()
                                    .uses(-1)
                                    .build()
                            ) {
                                if (it != viewer)
                                    return@callback
                                PlayerItemView.view(viewer, itemInMainHand)
                            }
                        }
                    } else if (content == "inv") {
                        component {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("player", sender.name)
                                    parsedPlaceholder(PlaceholderAPITagResolver(sender))
                                },
                                papi = false
                            ) {
                                BloraPlugin.configuration.chat.inventoryPlaceholderFormat
                            } with callback(
                                ClickCallback.Options.builder()
                                    .uses(-1)
                                    .build()
                            ) {
                                if (it != viewer)
                                    return@callback
                                PlayerInventoryView.view(viewer, sender)
                            } with showText {
                                localization(viewer) {
                                    this.chatViewInventoryTooltip
                                }
                            }
                        }
                    } else if (content == "enderchest") {
                        component {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("player", sender.name)
                                    parsedPlaceholder(PlaceholderAPITagResolver(sender))
                                },
                                papi = false
                            ) {
                                BloraPlugin.configuration.chat.enderChestPlaceholderFormat
                            } with callback(
                                ClickCallback.Options.builder()
                                    .uses(-1)
                                    .build()
                            ) {
                                if (it != viewer)
                                    return@callback
                                PlayerInventoryView.viewEnderChest(viewer, sender)
                            } with showText {
                                localization(viewer) {
                                    this.chatViewEnderChestTooltip
                                }
                            }
                        }
                    } else if (content.startsWith("cmd:") ||
                        (content.startsWith("cmd:\"/") && content.endsWith("\"")) ||
                        (content.startsWith("cmd:'/") && content.endsWith("'"))
                    ) {
                        val command = content.substring(4).let {
                            if ((it.startsWith("\"") && it.endsWith("\"")) || (it.startsWith("'") && it.endsWith("'"))) {
                                it.substring(1, it.length - 1)
                            } else {
                                it
                            }
                        }
                        component {
                            mini(BloraPlugin.configuration.chat.commandPlaceholderFormat) {
                                parsedPlaceholder("command", command)
                            } with suggestCommand(command)
                        }
                    } else if (content.startsWith("link:") && content.substring(5).matches(LinkTagResolver.URL_REGEX)) {
                        val link = content.substring(5)
                        component {
                            mini(BloraPlugin.configuration.chat.linkPlaceholderFormat) {
                                parsedPlaceholder("link", URLDecoder.decode(link, "UTF-8"))
                            } with openUrl(link)
                        }
                    } else if (content.startsWith("copy:")) {
                        val text = content.substring(5).let {
                            if ((it.startsWith("\"") && it.endsWith("\"")) || (it.startsWith("'") && it.endsWith("'"))) {
                                it.substring(1, it.length - 1)
                            } else {
                                it
                            }
                        }
                        component {
                            mini(BloraPlugin.configuration.chat.copyPlaceholderFormat) {
                                parsedPlaceholder("text", text)
                            } with suggestCommand(text)
                        }
                    } else {
                        for ((key, value) in BloraPlugin.configuration.chat.placeholders) {
                            if (content == key) {
                                return@replacement component {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder(PlaceholderAPITagResolver(sender))
                                        },
                                        papi = false
                                    ) {
                                        value
                                    }
                                }
                            }
                        }
                        Component.text(matchResult.group(0))
                    }
                }
            }
        }
        text { rawMessage }
    }

}