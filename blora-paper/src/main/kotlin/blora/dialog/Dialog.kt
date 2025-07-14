package blora.dialog

import blora.nms.toNMS
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.server.dialog.Dialog as NMSDialog

@Serializable
sealed class Dialog /*: DialogLike*/ {

    abstract fun toNBT(): NbtCompound

    fun toNms(): NMSDialog {
        return NMSDialog.DIRECT_CODEC.decode(
            NbtOps.INSTANCE,
            this.toNBT().toNMS()
        ).result().get().first
    }

}