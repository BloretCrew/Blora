package net.deechael.blora.dialog.action

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound

@Serializable
sealed class ClickType {

    abstract fun toNBT(): NbtCompound

}