package blora.dialog

import kotlinx.serialization.Serializable
import net.minecraft.server.dialog.Dialog as NMSDialog

@Serializable
sealed class Dialog /*: DialogLike*/ {

    abstract fun toNms(): NMSDialog

}