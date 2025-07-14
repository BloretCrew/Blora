package blora.chat

import blora.listener.packet.CustomClickActionHandler
import blora.util.randomString
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.event.ClickEvent

object CustomClickEvent {

    fun customClickEvent(callback: () -> Unit): ClickEvent {
        val identifier = randomString(32) + System.currentTimeMillis().toString()
        CustomClickActionHandler.resolving[identifier] = {
            callback()
        }
        return ClickEvent.custom(
            Key.key("blora", "custom_click"),
            BinaryTagHolder.binaryTagHolder(
                CompoundBinaryTag.builder()
                    .putString("identifier", identifier)
                    .build()
                    .toString()
            )
        )
    }

}