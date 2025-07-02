package net.deechael.blora.dialog.body

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound

@Serializable
sealed class DialogBody {

    abstract fun toNBT(): NbtCompound

}