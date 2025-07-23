package blora.listener

import blora.adventure.PlaceholderAPITagResolver
import blora.chat.*
import blora.extension.asDisplayName
import blora.extension.localization
import blora.extension.sendPacket
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.*
import plutoproject.adventurekt.text.style.WithStyle
import plutoproject.adventurekt.text.style.callback
import plutoproject.adventurekt.text.style.showText

object ChatListener : Listener {

    private val PRIVATE_MESSAGE_COMMANDS = listOf(
        "tell",
        "w",
        "whisper",
        "msg",
        "blora:tell",
        "blora:w",
        "blora:whisper",
        "blora:msg"
    )

    private fun listAllPlaceholders(player: Player): List<String> {
        return buildList {
            this.add("<item>")
            this.add("<inv>")
            this.add("<enderchest>")
            this.add("<cmd:...>")
            this.add("<link:...>")
            this.add("<copy:...>")
            for ((key, _) in BloraPlugin.configuration.chat.placeholders) {
                this.add("<$key>")
            }
            if (BloraPlugin.configuration.chat.mentionAllKeyword.isNotEmpty() && BloraPlugin.configuration.chat.mentionAllKeyword.isNotBlank() && player.hasPermission(
                    Permissions.Chat.MentionAll
                )
            ) {
                this.add("@${BloraPlugin.configuration.chat.mentionAllKeyword}")
            }
            Bukkit.getOnlinePlayers()
                .filter { it != player }
                .map { "@${it.name}" }
                .forEach(this::add)
        }
    }

    private fun updateCompletionsList() {
        Bukkit.getOnlinePlayers().forEach {
            it.sendPacket(
                ClientboundCustomChatCompletionsPacket(
                    ClientboundCustomChatCompletionsPacket.Action.SET,
                    listAllPlaceholders(it)
                )
            )
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        this.updateCompletionsList()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        this.updateCompletionsList()
    }

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        if (event.isCancelled) { // if the event is already cancelled by other plugins
            return
        }
        event.isCancelled = true // take the event by blora plugin
        if (event.player.scoreboardTags.contains(BloraPlugin.configuration.chat.muteTag)) {
            event.player.send {
                localization(event.player) {
                    this.chatErrorMuted
                }
            }
            return
        }
        val canMentionAll = event.player.hasPermission(Permissions.Chat.MentionAll)
        for (viewer in Bukkit.getOnlinePlayers()) {
            var rawMessage = event.message
            val messageIsMentionOther: String? = Bukkit.getOnlinePlayers()
                .filter { it != viewer }
                .find { rawMessage == "@${it.name}" }
                ?.name
            if (rawMessage == "@${BloraPlugin.configuration.chat.mentionAllKeyword}" && event.player.hasPermission(
                    Permissions.Chat.MentionAll
                )
            ) {
                if (BloraPlugin.configuration.chat.titleWhenMentioned) {
                    viewer.showTitle(
                        Title.title(
                            component {
                                localization(viewer) {
                                    this.chatMentionTitle
                                }
                            },
                            component {
                            }
                        )
                    )
                }
                if (BloraPlugin.configuration.chat.soundWhenMentioned) {
                    viewer.playSound(
                        Sound.sound(
                            Key.key("minecraft", "entity.experience_orb.pickup"),
                            Sound.Source.AMBIENT,
                            1f,
                            1f
                        )
                    )
                }
                viewer.send {
                    localization(
                        player = viewer,
                        tags = {
                            if (rawMessage.contains("<item>")) { // only add placeholder when need to reduce memory usage
                                val itemInMainHand = event.player.inventory.itemInMainHand
                                if (!itemInMainHand.type.isAir) {
                                    componentPlaceholder("item") {
                                        localization(
                                            player = viewer,
                                            tags = {
                                                componentPlaceholder("item") {
                                                    raw { itemInMainHand.asDisplayName() }
                                                }
                                                parsedPlaceholder(PlaceholderAPITagResolver(event.player))
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
                                            parsedPlaceholder("player", event.player.name)
                                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                        },
                                        papi = false
                                    ) {
                                        BloraPlugin.configuration.chat.inventoryPlaceholderFormat
                                    } with callback {
                                        if (it != viewer)
                                            return@callback
                                        PlayerInventoryView.view(viewer, event.player)
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
                                            parsedPlaceholder("player", event.player.name)
                                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                        },
                                        papi = false
                                    ) {
                                        BloraPlugin.configuration.chat.enderChestPlaceholderFormat
                                    } with callback {
                                        if (it != viewer)
                                            return@callback
                                        PlayerInventoryView.viewEnderChest(viewer, event.player)
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

                            parsedPlaceholder("message", BloraPlugin.configuration.chat.mentionAllFormat)
                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                        },
                        papi = false
                    ) {
                        BloraPlugin.configuration.chat.format
                    }
                }
            } else if (rawMessage == "@${viewer.name}" && viewer != event.player) {
                if (BloraPlugin.configuration.chat.titleWhenMentioned) {
                    viewer.showTitle(
                        Title.title(
                            component {
                                localization(viewer) {
                                    this.chatMentionTitle
                                }
                            },
                            component {
                            }
                        )
                    )
                }
                if (BloraPlugin.configuration.chat.soundWhenMentioned) {
                    viewer.playSound(
                        Sound.sound(
                            Key.key("minecraft", "entity.experience_orb.pickup"),
                            Sound.Source.AMBIENT,
                            1f,
                            1f
                        )
                    )
                }
                viewer.send {
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder(
                                "message",
                                BloraPlugin.configuration.chat.mentionSelfFormat.replace("<mentioned>", viewer.name)
                            )
                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                        },
                        papi = false
                    ) {
                        BloraPlugin.configuration.chat.format
                    }
                }
            } else if (messageIsMentionOther != null) {
                viewer.send {
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder(
                                "message",
                                BloraPlugin.configuration.chat.mentionOtherFormat.replace(
                                    "<mentioned>",
                                    messageIsMentionOther
                                )
                            )
                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                        },
                        papi = false
                    ) {
                        BloraPlugin.configuration.chat.format
                    }
                }
            } else {
                // mention self
                // here add a check "viewer != event.player" so players cannot mention themselves
                val mentionSelfAtStart = rawMessage.startsWith("@${viewer.name} ") && viewer != event.player
                val mentionSelfAtEnd = rawMessage.endsWith(" @${viewer.name}") && viewer != event.player
                val mentionSelfInMessage = rawMessage.contains(" @${viewer.name} ") && viewer != event.player

                if (mentionSelfAtStart) {
                    rawMessage = rawMessage.substring("@${viewer.name}".length)
                }
                if (mentionSelfAtEnd) {
                    rawMessage = rawMessage.substring(0, rawMessage.length - "@${viewer.name}".length)
                }

                // mention all
                val mentionAllKeyword = BloraPlugin.configuration.chat.mentionAllKeyword
                val mentionAllAtStart =
                    rawMessage.startsWith("@$mentionAllKeyword ") && mentionAllKeyword.isNotBlank() && mentionAllKeyword.isNotEmpty() && canMentionAll
                val mentionAllAtEnd =
                    rawMessage.endsWith(" @$mentionAllKeyword") && mentionAllKeyword.isNotBlank() && mentionAllKeyword.isNotEmpty() && canMentionAll
                val mentionAllInMessage =
                    rawMessage.contains(" @$mentionAllKeyword ") && mentionAllKeyword.isNotBlank() && mentionAllKeyword.isNotEmpty() && canMentionAll

                if (mentionAllAtStart) {
                    rawMessage = rawMessage.substring("@$mentionAllKeyword".length)
                }
                if (mentionAllAtEnd) {
                    rawMessage = rawMessage.substring(0, rawMessage.length - "@$mentionAllKeyword".length)
                }

                // mention others
                val mentionedOtherAtStart: String? = Bukkit.getOnlinePlayers()
                    .filter { it != viewer }
                    .find { rawMessage.startsWith("@${it.name} ") }?.name
                val mentionedOtherAtEnd: String? = Bukkit.getOnlinePlayers()
                    .filter { it != viewer }
                    .find { rawMessage.endsWith(" @${it.name}") }?.name
                val mentionedOtherInMessage: List<String> = Bukkit.getOnlinePlayers()
                    .filter { it != viewer }
                    .filter { rawMessage.contains(" @${it.name} ") }
                    .map { it.name }
                    .toList()

                if (mentionedOtherAtStart != null) {
                    rawMessage = rawMessage.substring("@$mentionedOtherAtStart".length)
                }
                if (mentionedOtherAtEnd != null) {
                    rawMessage = rawMessage.substring(0, rawMessage.length - "@$mentionedOtherAtEnd".length)
                }

                val mentioned = mentionSelfAtStart || mentionSelfInMessage || mentionSelfAtEnd
                        || mentionAllAtStart || mentionAllInMessage || mentionAllAtEnd

                if (BloraPlugin.configuration.chat.titleWhenMentioned && mentioned) {
                    viewer.showTitle(
                        Title.title(
                            component {
                                localization(viewer) {
                                    this.chatMentionTitle
                                }
                            },
                            component {
                            }
                        )
                    )
                }
                if (BloraPlugin.configuration.chat.soundWhenMentioned && mentioned) {
                    viewer.playSound(
                        Sound.sound(
                            Key.key("minecraft", "entity.experience_orb.pickup"),
                            Sound.Source.AMBIENT,
                            1f,
                            1f
                        )
                    )
                }
                viewer.send {
                    localization(
                        player = viewer,
                        tags = {
                            componentPlaceholder("message") {
                                if (mentionSelfAtStart) {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("mentioned", viewer.name)
                                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                        },
                                        papi = false
                                    ) {
                                        BloraPlugin.configuration.chat.mentionSelfFormat
                                    }
                                }
                                if (mentionAllAtStart) {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                        },
                                        papi = false
                                    ) {
                                        BloraPlugin.configuration.chat.mentionAllFormat
                                    }
                                }
                                if (mentionedOtherAtStart != null) {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("mentioned", mentionedOtherAtStart)
                                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                        },
                                        papi = false
                                    ) {
                                        BloraPlugin.configuration.chat.mentionOtherFormat
                                    }
                                }
                                raw {
                                    if (event.player.hasPermission(Permissions.Chat.MiniMessage)) {
                                        Chatting.miniMessageSupport(event.player, viewer, rawMessage) {
                                            if (mentionSelfInMessage) {
                                                replacement {
                                                    matchLiteral(" @${viewer.name} ")
                                                    replace {
                                                        space()
                                                        localization(
                                                            player = viewer,
                                                            tags = {
                                                                parsedPlaceholder("mentioned", viewer.name)
                                                                parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                                            },
                                                            papi = false
                                                        ) {
                                                            BloraPlugin.configuration.chat.mentionSelfFormat
                                                        }
                                                        space()
                                                    }
                                                }
                                            }
                                            if (mentionAllInMessage) {
                                                replacement {
                                                    matchLiteral(" @$mentionAllKeyword ")
                                                    replace {
                                                        space()
                                                        localization(
                                                            player = viewer,
                                                            tags = {
                                                                parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                                            },
                                                            papi = false
                                                        ) {
                                                            BloraPlugin.configuration.chat.mentionAllFormat
                                                        }
                                                        space()
                                                    }
                                                }
                                            }
                                            for (mentionedOther in mentionedOtherInMessage) {
                                                replacement {
                                                    matchLiteral(" @$mentionedOther ")
                                                    replace {
                                                        space()
                                                        localization(
                                                            player = viewer,
                                                            tags = {
                                                                parsedPlaceholder("mentioned", mentionedOther)
                                                                parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                                            },
                                                            papi = false
                                                        ) {
                                                            BloraPlugin.configuration.chat.mentionOtherFormat
                                                        }
                                                        space()
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Chatting.noMiniMessageSupport(event.player, viewer, rawMessage) {
                                            if (mentionSelfInMessage) {
                                                replacement {
                                                    matchLiteral(" @${viewer.name} ")
                                                    replace {
                                                        space()
                                                        localization(
                                                            player = viewer,
                                                            tags = {
                                                                parsedPlaceholder("mentioned", viewer.name)
                                                                parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                                            },
                                                            papi = false
                                                        ) {
                                                            BloraPlugin.configuration.chat.mentionSelfFormat
                                                        }
                                                        space()
                                                    }
                                                }
                                            }
                                            if (mentionAllInMessage) {
                                                replacement {
                                                    matchLiteral(" @$mentionAllKeyword ")
                                                    replace {
                                                        space()
                                                        localization(
                                                            player = viewer,
                                                            tags = {
                                                                parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                                            },
                                                            papi = false
                                                        ) {
                                                            BloraPlugin.configuration.chat.mentionAllFormat
                                                        }
                                                        space()
                                                    }
                                                }
                                            }
                                            for (mentionedOther in mentionedOtherInMessage) {
                                                replacement {
                                                    matchLiteral(" @$mentionedOther ")
                                                    replace {
                                                        space()
                                                        localization(
                                                            player = viewer,
                                                            tags = {
                                                                parsedPlaceholder("mentioned", mentionedOther)
                                                                parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                                            },
                                                            papi = false
                                                        ) {
                                                            BloraPlugin.configuration.chat.mentionOtherFormat
                                                        }
                                                        space()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (mentionSelfAtEnd) {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("mentioned", viewer.name)
                                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                        },
                                        papi = false
                                    ) {
                                        BloraPlugin.configuration.chat.mentionSelfFormat
                                    }
                                }
                                if (mentionAllAtEnd) {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                        },
                                        papi = false
                                    ) {
                                        BloraPlugin.configuration.chat.mentionAllFormat
                                    }
                                }
                                if (mentionedOtherAtEnd != null) {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("mentioned", mentionedOtherAtEnd)
                                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                                        },
                                        papi = false
                                    ) {
                                        BloraPlugin.configuration.chat.mentionOtherFormat
                                    }
                                }
                            }
                            parsedPlaceholder(PlaceholderAPITagResolver(event.player))
                        },
                        papi = false
                    ) {
                        BloraPlugin.configuration.chat.format
                    }
                }
            }
        }

        Bukkit.getConsoleSender().send {
            text { event.player.name }
            text { ": " }
            text { event.message }
        }
    }

    @EventHandler
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val rawCommand = if (event.message.startsWith("/"))
            event.message.substring(1).trim()
        else
            event.message.trim()
        if (rawCommand.contains(" ")) {
            val split = rawCommand.split(" ")
            val command = split[0]
            if (PRIVATE_MESSAGE_COMMANDS.any { it.contentEquals(command.lowercase(), true) }) {
                event.isCancelled = true
            }
        } else {
            if (PRIVATE_MESSAGE_COMMANDS.any { it.contentEquals(rawCommand.lowercase(), true) }) {
                event.isCancelled = true
            }
        }
    }

}