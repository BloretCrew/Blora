package blora.api.player

import blora.api.command.CommandInvoker
import blora.dialog.Dialog
import blora.extension.sendPacket
import net.minecraft.core.Holder
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket
import org.bukkit.entity.Player

interface BloraPlayer : CommandInvoker {

    override val asBukkit: Player

    fun sendDialog(dialog: Dialog) {
        this.asBukkit.sendPacket(ClientboundShowDialogPacket(Holder.direct(dialog.toNms())))
    }

}