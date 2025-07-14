package blora.modules.mail

import blora.extension.localization
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import java.util.*
import org.bukkit.entity.Player as BukkitPlayer

sealed class Sender {

    class Player(val uuid: UUID) : Sender() {

        override fun getDisplayName(viewer: BukkitPlayer): Component {
            return Component.empty()
        }

        override fun serialize(): String {
            return "player:${this.uuid}"
        }

    }

    class System(val name: String) : Sender() {

        override fun getDisplayName(viewer: BukkitPlayer): Component {
            return component {
                localization(viewer) {
                    name
                }
            }
        }

        override fun serialize(): String {
            return "system:${this.name}"
        }

    }

    object Unknown : Sender() {

        override fun getDisplayName(viewer: BukkitPlayer): Component {
            return component {
                localization(viewer) {
                    this.mailSenderUnknown
                }
            }
        }

        override fun serialize(): String {
            return "unknown"
        }

    }

    companion object {

        fun deserialize(value: String): Sender {
            return if (value.startsWith("player")) {
                Player(UUID.fromString(value.substring(7))!!)
            } else if (value.startsWith("system")) {
                System(value.substring(7))
            } else {
                Unknown
            }
        }

    }

    abstract fun getDisplayName(viewer: BukkitPlayer): Component

    abstract fun serialize(): String

}