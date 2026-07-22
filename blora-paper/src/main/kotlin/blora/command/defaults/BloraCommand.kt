package blora.command.defaults

import blora.extension.localization
import blora.internal.api.command.*
import blora.internal.api.command.argument.Arguments
import blora.messaging.packet.common.DebugMessagePacket
import blora.messaging.packet.common.ReloadConfigurationPacket
import blora.nms.toNMS
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import blora.serialization.nbt.compound
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mojang.serialization.JsonOps
import net.benwoodworth.knbt.NbtByte
import net.benwoodworth.knbt.NbtByteArray
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtDouble
import net.benwoodworth.knbt.NbtFloat
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtIntArray
import net.benwoodworth.knbt.NbtList
import net.benwoodworth.knbt.NbtLong
import net.benwoodworth.knbt.NbtLongArray
import net.benwoodworth.knbt.NbtShort
import net.benwoodworth.knbt.NbtString
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import org.bukkit.entity.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.text

object BloraCommand {

    fun register() {
        BloraCommandLib.registerCommand("blora") {
            meta {
                alias("bloret")
                alias("blg")
                alias("bailuogu")
            }
            requires {
                return@requires this.asBukkit.hasPermission(Permissions.Commands.Blora)
                        || this.asBukkit.hasPermission(Permissions.Admin)
            }

            literal("proxy") {
                literal("debug") {
                    requires {
                        // 只有 Rhedar 和 DeeChael 能使用调试用途的命令哦
                        return@requires (this.asBukkit.name.lowercase().contentEquals("deechael") ||
                                this.asBukkit.name.lowercase().contentEquals("rhedar") || this.isConsole)
                    }
                    argument("message", Arguments.greedyString) { messageFunc ->
                        executor {
                            val message = messageFunc()
                            BloraPlugin.client.send(DebugMessagePacket().apply { this.message = message })
                        }
                    }
                }
                literal("reconnect") {
                    executor {
                        if (BloraPlugin.client.isConnected()) {
                            this.invoker.sendMessage {
                                text { "已连接，无需重连" }
                            }
                            return@executor
                        }
                        BloraPlugin.client.reconnect()
                        this.invoker.sendMessage {
                            text { "已尝试重连" }
                        }
                    }
                }
            }

            literal("reload") {
                executor {
                    // Reload this server immediately, then notify proxy / other backends.
                    BloraPlugin.reloadConfiguration()
                    val player = if (this.invoker.isPlayer) this.invoker.asBukkit as? Player else null
                    if (BloraPlugin.client.isConnected()) {
                        BloraPlugin.client.send(ReloadConfigurationPacket)
                        this.invoker.sendMessage {
                            localization(player) {
                                this.command.commandReloadSuccessSynced
                            }
                        }
                    } else {
                        this.invoker.sendMessage {
                            localization(player) {
                                this.command.commandReloadSuccessLocal_only
                            }
                        }
                    }
                }
            }

            literal("debug") {
                requires {
                    // 只有 Rhedar 和 DeeChael 能使用调试用途的命令哦
                    return@requires (this.asBukkit.name.lowercase().contentEquals("deechael") ||
                            this.asBukkit.name.lowercase().contentEquals("rhedar") || this.isConsole)
                }

                literal("nbt") {
                    executor {
                        val knbt = compound {
                            "nbt_string" eq NbtString("aaa")
                            "nbt_byte" eq NbtByte(1)
                            "nbt_short" eq NbtShort(1)
                            "nbt_int" eq NbtInt(1)
                            "nbt_long" eq NbtLong(1)
                            "nbt_float" eq NbtFloat(1f)
                            "nbt_double" eq NbtDouble(1.0)
                            "nbt_compound" eq compound {
                                "key" eq "value"
                            }
                            "nbt_list" eq NbtList(
                                listOf(
                                    compound {
                                        "key" eq "value1"
                                    },
                                    compound {
                                        "key" eq "value2"
                                    }
                                )
                            )
                            "nbt_byte_array" eq NbtByteArray(
                                byteArrayOf(
                                    0, 1, 2, 3
                                )
                            )
                            "nbt_int_array" eq NbtIntArray(
                                intArrayOf(
                                    0, 1, 2, 3
                                )
                            )
                            "nbt_long_array" eq NbtLongArray(
                                longArrayOf(
                                    0, 1, 2, 3
                                )
                            )
                        }

                        CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, knbt.toNMS() as CompoundTag)
                            .result()
                            .ifPresent { jsonElement ->
                                this.invoker.sendMessage {
                                    text {
                                        GsonBuilder()
                                            .setPrettyPrinting()
                                            .create()
                                            .toJson(jsonElement)
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

}