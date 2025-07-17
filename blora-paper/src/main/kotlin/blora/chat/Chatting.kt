package blora.chat

import blora.adventure.PlaceholderAPITagResolver
import blora.extension.asDisplayName
import blora.extension.localization
import blora.plugin.BloraPlugin
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.*
import plutoproject.adventurekt.text.style.WithStyle
import plutoproject.adventurekt.text.style.callback
import plutoproject.adventurekt.text.style.showText

object Chatting {

    fun miniMessageSupport(
        sender: Player,
        viewer: Player,
        rawMessage: String,
        replacements: ComponentReplacements.() -> Unit
    ): Component = component {
        replacements(replacements)
        localization(
            player = viewer,
            tags = {
                if (rawMessage.contains("<item>")) { // only add placeholder when need to reduce memory usage
                    val itemInMainHand = sender.inventory.itemInMainHand
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
                            } with callback {
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
                        } with callback {
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
                        } with callback {
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
        replacements: ComponentReplacements.() -> Unit
    ): Component = component {
        replacements {
            replacements
            if (rawMessage.contains("<item>")) {
                val itemInMainHand = sender.inventory.itemInMainHand
                if (!itemInMainHand.type.isAir) {
                    replacement {
                        matchLiteral("<item>")
                        replace {
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
                            } with callback {
                                if (it != viewer)
                                    return@callback
                                PlayerItemView.view(viewer, itemInMainHand)
                            }
                        }
                    }
                }
            }
            if (rawMessage.contains("<inv>")) {
                replacement {
                    matchLiteral("<inv>")
                    replace {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("player", sender.name)
                                parsedPlaceholder(PlaceholderAPITagResolver(sender))
                            },
                            papi = false
                        ) {
                            BloraPlugin.configuration.chat.inventoryPlaceholderFormat
                        } with callback {
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
            }
            if (rawMessage.contains("<enderchest>")) {
                replacement {
                    matchLiteral("<enderchest>")
                    replace {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("player", sender.name)
                                parsedPlaceholder(PlaceholderAPITagResolver(sender))
                            },
                            papi = false
                        ) {
                            BloraPlugin.configuration.chat.enderChestPlaceholderFormat
                        } with callback {
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
            }
            replacement {
                match("<cmd:(/.*)>")
                replacement { matchResult, builder ->
                    return@replacement component {
                        mini(BloraPlugin.configuration.chat.commandPlaceholderFormat) {
                            parsedPlaceholder("command", matchResult.group(1))
                        }
                    }
                }
            }
            replacement {
                match("<link:((https?://)?([\\w-]+\\.)+[\\w-]+(:\\d+)?(/[\\w\\-.~!*'();:@&=+\$,?#/]*)?)>")
                replacement { matchResult, builder ->
                    return@replacement component {
                        mini(BloraPlugin.configuration.chat.linkPlaceholderFormat) {
                            parsedPlaceholder("link", matchResult.group(1))
                        }
                    }
                }
            }
            replacement {
                match("<copy:(.*)>")
                replacement { matchResult, builder ->
                    return@replacement component {
                        mini(BloraPlugin.configuration.chat.copyPlaceholderFormat) {
                            parsedPlaceholder("text", matchResult.group(1))
                        }
                    }
                }
            }
            for ((key, value) in BloraPlugin.configuration.chat.placeholders) {
                replacement {
                    matchLiteral(key)
                    replace {
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
        }
        text { rawMessage }
    }

}