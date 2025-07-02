package net.deechael.blora.dialog.input

import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound

@Serializable
sealed class InputControl {

    abstract fun toNBT(): NbtCompound

}