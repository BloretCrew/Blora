package blora.listener

import blora.extension.localization
import blora.modules.mail.MailModule
import blora.plugin.BloraPlugin
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.style.runCommand
import plutoproject.adventurekt.text.style.showText
import plutoproject.adventurekt.text.with

object SystemMailListener : Listener {

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

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val systemMails = BloraPlugin.database
            .listSystemMails()
            .filter { !BloraPlugin.database.isPlayerReceivedSystemMail(player.uniqueId, it.identifier) }
            .filter { it.actived }
            .toList()
        for (systemMail in systemMails) {
            if (systemMail.parsedTriggers.isEmpty()) // no trigger so don't add to player
                continue
            var shouldContinue = false
            for (trigger in systemMail.parsedTriggers) {
                val result = trigger.check(player)
                if (!result) {
                    if (trigger.shouldNeverAcquirableAfterCheck) {
                        MailModule.receiveNewMail(
                            player,
                            systemMail,
                            visible = false,
                            notify = false
                        )
                    }
                    shouldContinue = true
                    break
                }
            }
            if (shouldContinue)
                continue
            MailModule.receiveNewMail(player, systemMail, visible = true, notify = false)
        }
        if (BloraPlugin.configuration.mail.unreadTips) {
            val amount = BloraPlugin.database
                .getMailsByReceiverUuid(player.uniqueId)
                .filter { it.visible }
                .filter { !it.isRead }
                .toList()
                .size
            if (amount < 1)
                return
            player.send {
                localization(
                    player = player,
                    tags = {
                        parsedPlaceholder(
                            "amount",
                            amount.toString()
                        )
                    }
                ) {
                    this.mailJoinUnreadTips
                } with runCommand("/mail") with showText {
                    localization(player) {
                        this.mailJoinUnreadTipsHover
                    }
                }
            }
        }
    }

}