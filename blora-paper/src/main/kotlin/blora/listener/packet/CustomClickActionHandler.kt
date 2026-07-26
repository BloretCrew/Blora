package blora.listener.packet

import blora.internal.api.scheduler.BukkitMain
import blora.nms.toKnbt
import blora.plugin.BloraPlugin
import blora.serialization.nbt.compound
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.benwoodworth.knbt.NbtTag
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
import kotlin.jvm.optionals.getOrNull

object CustomClickActionHandler {

    // SupervisorJob: one failed dialog callback must not cancel the whole scope,
    // otherwise every subsequent custom-click (mail/redeem dialogs) silently dies until restart.
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        BloraPlugin.slF4JLogger.error(
            "Uncaught exception in custom click action scope",
            throwable
        )
    }
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.BukkitMain + exceptionHandler)
    val resolving = mutableMapOf<String, (NbtTag?) -> Unit>()

    fun handle(packet: ServerboundCustomClickActionPacket) {
        scope.launch {
            try {
                val id = PaperAdventure.asAdventure(packet.id)

                if (id.asString() != "blora:custom_click") {
                    return@launch
                }

                val tag = packet.payload.getOrNull().toKnbt() as NbtCompound

                val realTag = tag["realTag"]

                val finalTag = compound { // this step is to deliver input values
                    for ((key, value) in tag) {
                        if (key == "identifier")
                            continue
                        if (key == "realTag")
                            continue
                        key eq value
                    }
                    if (realTag != null && realTag is NbtCompound) {
                        for ((key, value) in realTag) {
                            key eq value
                        }
                    }
                }

                val identifier = (tag["identifier"] as NbtString).value
                val callback = resolving[identifier]
                if (callback != null) {
                    resolving.remove(identifier)
                    try {
                        callback(finalTag)
                    } catch (ex: Exception) {
                        // Never swallow cooperative cancellation.
                        if (ex is CancellationException) throw ex
                        // Log per-callback failures so admins see them instead of "button does nothing"
                        BloraPlugin.slF4JLogger.error(
                            "Custom click callback failed (identifier=$identifier)",
                            ex
                        )
                    }
                }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                BloraPlugin.slF4JLogger.error("Failed to handle custom click action packet", ex)
            }
        }
    }

}