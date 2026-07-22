package blora.listener

import blora.adventure.PlaceholderAPITagResolver
import blora.chat.*
import blora.extension.localization
import blora.extension.sendPacket
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.title.Title
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.*

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

    private val MENTION_SOUND = Sound.sound(
        Key.key("minecraft", "entity.experience_orb.pickup"),
        Sound.Source.AMBIENT,
        1f,
        1f
    )

    private var registered: Boolean = false

    fun register() {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
            registered = true
        }
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
        this.registered = false
    }

    private fun staticCompletions(): List<String> {
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
        }
    }

    private fun listAllPlaceholders(player: Player, onlineNames: Collection<String>): List<String> {
        return buildList {
            this.addAll(staticCompletions())
            if (BloraPlugin.configuration.chat.mentionAllKeyword.isNotBlank() &&
                player.hasPermission(Permissions.Chat.MentionAll)
            ) {
                this.add("@${BloraPlugin.configuration.chat.mentionAllKeyword}")
            }
            for (name in onlineNames) {
                if (name != player.name) {
                    this.add("@$name")
                }
            }
        }
    }

    private fun setCompletions(player: Player, onlineNames: Collection<String>) {
        player.sendPacket(
            ClientboundCustomChatCompletionsPacket(
                ClientboundCustomChatCompletionsPacket.Action.SET,
                listAllPlaceholders(player, onlineNames)
            )
        )
    }

    private fun patchCompletions(player: Player, action: ClientboundCustomChatCompletionsPacket.Action, entries: List<String>) {
        if (entries.isEmpty()) {
            return
        }
        player.sendPacket(ClientboundCustomChatCompletionsPacket(action, entries))
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val joiner = event.player
        val onlineNames = Bukkit.getOnlinePlayers().map { it.name }
        // Full list for the joiner; incremental ADD of @joiner for everyone else (O(n) total).
        setCompletions(joiner, onlineNames)
        val added = listOf("@${joiner.name}")
        for (other in Bukkit.getOnlinePlayers()) {
            if (other != joiner) {
                patchCompletions(other, ClientboundCustomChatCompletionsPacket.Action.ADD, added)
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val quitter = event.player
        val removed = listOf("@${quitter.name}")
        for (other in Bukkit.getOnlinePlayers()) {
            if (other != quitter) {
                patchCompletions(other, ClientboundCustomChatCompletionsPacket.Action.REMOVE, removed)
            }
        }
    }

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        if (event.isCancelled) {
            return
        }
        event.isCancelled = true
        if (event.player.scoreboardTags.contains(BloraPlugin.configuration.chat.muteTag)) {
            event.player.send {
                localization(event.player) {
                    this.chatErrorMuted
                }
            }
            return
        }

        val sender = event.player
        val rawMessage = event.message
        // Snapshot once: avoid O(n) getOnlinePlayers / inventory reads inside the hot path.
        val onlinePlayers = Bukkit.getOnlinePlayers().toList()
        val onlineByName = HashMap<String, Player>(onlinePlayers.size * 2)
        for (player in onlinePlayers) {
            onlineByName[player.name] = player
        }
        val itemInMainHand = sender.inventory.itemInMainHand.clone()
        // Capture inv/enderchest once at send time; store by id with TTL for later clicks.
        val inventorySnapshotId =
            if (rawMessage.contains("<inv>")) {
                PlayerInventoryView.storeInventory(PlayerInventoryView.captureInventory(sender))
            } else null
        val enderChestSnapshotId =
            if (rawMessage.contains("<enderchest>")) {
                PlayerInventoryView.storeEnderChest(PlayerInventoryView.captureEnderChest(sender))
            } else null
        val canMentionAll = sender.hasPermission(Permissions.Chat.MentionAll)
        val hasMiniMessage = sender.hasPermission(Permissions.Chat.MiniMessage)
        val chatConfig = BloraPlugin.configuration.chat
        val mentionAllKeyword = chatConfig.mentionAllKeyword

        // Event is already async — process inline (no per-viewer coroutine / scheduler hop).
        // Deliver to sender first so their own message appears ASAP.
        val viewers = ArrayList<Player>(onlinePlayers.size)
        if (onlineByName.containsKey(sender.name)) {
            viewers.add(sender)
        }
        for (player in onlinePlayers) {
            if (player != sender) {
                viewers.add(player)
            }
        }

        // Longer names first so "@Alexander" wins over prefix "@Alex".
        val mentionNamesByLength = onlineByName.keys.sortedByDescending { it.length }

        for (viewer in viewers) {
            deliverChat(
                sender = sender,
                viewer = viewer,
                rawMessage = rawMessage,
                mentionNamesByLength = mentionNamesByLength,
                itemInMainHand = itemInMainHand,
                inventorySnapshotId = inventorySnapshotId,
                enderChestSnapshotId = enderChestSnapshotId,
                canMentionAll = canMentionAll,
                hasMiniMessage = hasMiniMessage,
                mentionAllKeyword = mentionAllKeyword,
            )
        }

        Bukkit.getConsoleSender().send {
            text { sender.name }
            text { ": " }
            text { rawMessage }
        }
    }

    private fun notifyMentioned(viewer: Player) {
        val chatConfig = BloraPlugin.configuration.chat
        if (chatConfig.titleWhenMentioned) {
            viewer.showTitle(
                Title.title(
                    component {
                        localization(viewer) {
                            this.chatMentionTitle
                        }
                    },
                    component { }
                )
            )
        }
        if (chatConfig.soundWhenMentioned) {
            viewer.playSound(MENTION_SOUND)
        }
    }

    /**
     * True when [token] appears as a full @mention (not a prefix of a longer name token).
     * No leading whitespace required; only a trailing name-char boundary is checked
     * (end of string or non-[A-Za-z0-9_] after the token).
     */
    private fun containsMentionToken(message: String, token: String): Boolean {
        if (token.isEmpty() || !message.contains("@$token")) {
            return false
        }
        var from = 0
        val needle = "@$token"
        while (from <= message.length - needle.length) {
            val index = message.indexOf(needle, from)
            if (index < 0) {
                return false
            }
            val after = index + needle.length
            val boundaryAfter = after >= message.length || !isNameChar(message[after])
            if (boundaryAfter) {
                return true
            }
            from = index + 1
        }
        return false
    }

    private fun isNameChar(char: Char): Boolean {
        return char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char == '_'
    }

    /**
     * Regex for every "@name" occurrence. Does not consume surrounding spaces, so
     * consecutive "@A @A @A" (or glued "@A@A") all highlight independently.
     * Trailing (?!...) prevents matching a shorter name inside a longer one.
     */
    private fun mentionMatchPattern(token: String): String {
        return Regex.escape("@$token") + "(?![A-Za-z0-9_])"
    }

    /**
     * Rebuild chat completions for every online player (e.g. after config reload).
     */
    fun refreshAllCompletions() {
        val online = Bukkit.getOnlinePlayers()
        if (online.isEmpty()) {
            return
        }
        val onlineNames = online.map { it.name }
        for (player in online) {
            setCompletions(player, onlineNames)
        }
    }

    private fun deliverChat(
        sender: Player,
        viewer: Player,
        rawMessage: String,
        mentionNamesByLength: List<String>,
        itemInMainHand: ItemStack,
        inventorySnapshotId: java.util.UUID?,
        enderChestSnapshotId: java.util.UUID?,
        canMentionAll: Boolean,
        hasMiniMessage: Boolean,
        mentionAllKeyword: String,
    ) {
        val chatConfig = BloraPlugin.configuration.chat
        val senderPapi = PlaceholderAPITagResolver(sender)
        val mentionAllEnabled = mentionAllKeyword.isNotBlank() && canMentionAll

        // Title/sound: only when this viewer is @'d, or @all is used (same as before).
        val viewerMentioned = viewer != sender && containsMentionToken(rawMessage, viewer.name)
        val allMentioned = mentionAllEnabled && containsMentionToken(rawMessage, mentionAllKeyword)
        if (viewerMentioned || allMentioned) {
            notifyMentioned(viewer)
        }

        // Collect which @tokens actually appear — still one rule per name, but the rule
        // replaces EVERY occurrence (not "once only").
        val presentNames = mentionNamesByLength.filter { containsMentionToken(rawMessage, it) }

        viewer.send {
            localization(
                player = viewer,
                tags = {
                    componentPlaceholder("message") {
                        raw {
                            val mentionReplacements: ComponentReplacements.() -> Unit = {
                                // @all before player names if keyword could be a name prefix (unlikely but safe).
                                if (allMentioned) {
                                    replacement {
                                        match(mentionMatchPattern(mentionAllKeyword))
                                        replace {
                                            localization(
                                                player = viewer,
                                                tags = {
                                                    parsedPlaceholder(senderPapi)
                                                },
                                                papi = false
                                            ) {
                                                chatConfig.mentionAllFormat
                                            }
                                        }
                                    }
                                }
                                for (name in presentNames) {
                                    // Sender @'ing themselves: keep plain text (old behavior).
                                    if (name == viewer.name && viewer == sender) {
                                        continue
                                    }
                                    val isSelf = name == viewer.name && viewer != sender
                                    replacement {
                                        match(mentionMatchPattern(name))
                                        replace {
                                            localization(
                                                player = viewer,
                                                tags = {
                                                    parsedPlaceholder("mentioned", name)
                                                    parsedPlaceholder(senderPapi)
                                                },
                                                papi = false
                                            ) {
                                                if (isSelf) {
                                                    chatConfig.mentionSelfFormat
                                                } else {
                                                    chatConfig.mentionOtherFormat
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (hasMiniMessage) {
                                Chatting.miniMessageSupport(
                                    sender = sender,
                                    viewer = viewer,
                                    rawMessage = rawMessage,
                                    itemInMainHand = itemInMainHand,
                                    inventorySnapshotId = inventorySnapshotId,
                                    enderChestSnapshotId = enderChestSnapshotId,
                                    replacements = mentionReplacements,
                                )
                            } else {
                                Chatting.noMiniMessageSupport(
                                    sender = sender,
                                    viewer = viewer,
                                    rawMessage = rawMessage,
                                    itemInMainHand = itemInMainHand,
                                    inventorySnapshotId = inventorySnapshotId,
                                    enderChestSnapshotId = enderChestSnapshotId,
                                    replacements = mentionReplacements,
                                )
                            }
                        }
                    }
                    parsedPlaceholder(senderPapi)
                },
                papi = false
            ) {
                chatConfig.format
            }
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
