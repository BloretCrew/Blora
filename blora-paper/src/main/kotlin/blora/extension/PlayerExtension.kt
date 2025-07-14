package blora.extension

import blora.dialog.Dialog
import net.minecraft.core.Holder
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

fun Player.sendPacket(packet: Packet<*>) {
    (this as CraftPlayer).handle.connection.send(packet)
}

fun Player.openDialog(dialog: Dialog) {
    this.sendPacket(ClientboundShowDialogPacket(Holder.direct(dialog.toNms())))
}