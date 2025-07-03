package blora.debug

import blora.api.command.QuickCommandLib
import blora.api.command.command
import blora.api.command.playerExecutor
import blora.plugin.BloraPlugin
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.injector.netty.WirePacket
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket
import net.minecraft.server.dialog.*
import net.minecraft.server.dialog.body.ItemBody
import net.minecraft.server.dialog.body.PlainMessage
import net.minecraft.server.dialog.input.TextInput
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.io.File
import java.util.*

object DialogDebuger {

    fun debug() {
        ProtocolLibrary.getProtocolManager()
            .addPacketListener(object : PacketAdapter(
                BloraPlugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.SHOW_DIALOG
            ) {
                override fun onPacketSending(event: PacketEvent) {

                    if (event.packetType == PacketType.Play.Server.SHOW_DIALOG) {
                        File(BloraPlugin.dataFolder, "debug_nms.nbt")
                            .writeBytes(WirePacket.bytesFromPacket(event.packet))
                    }
                }
            })

        QuickCommandLib.registerCommand(
            command("dialogtest") {
                playerExecutor {
                    val confirmation = ConfirmationDialog(
                        CommonDialogData(
                            Component.literal("test"),
                            Optional.empty(),
                            false,
                            false,
                            DialogAction.CLOSE,
                            listOf(
                                PlainMessage(
                                    Component.literal("test"),
                                    150
                                ),
                                PlainMessage(
                                    Component.literal("test2"),
                                    150
                                )
                            ),
                            listOf(
                                Input(
                                    "test",
                                    TextInput(
                                        150,
                                        Component.literal("test"),
                                        true,
                                        "aaa",
                                        150,
                                        Optional.of(
                                            TextInput.MultilineOptions(
                                                Optional.of(100),
                                                Optional.of(200)
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        ActionButton(
                            CommonButtonData(
                                Component.literal("yes"),
                                Optional.empty(),
                                150
                            ),
                            Optional.empty()
                        ),
                        ActionButton(
                            CommonButtonData(
                                Component.literal("no"),
                                Optional.empty(),
                                150
                            ),
                            Optional.empty()
                        )
                    )

                    val packet = ClientboundShowDialogPacket(Holder.direct(confirmation))

                    (this.invoker.asBukkit as CraftPlayer).handle.connection.send(packet)
                }
            }
        )
    }

}