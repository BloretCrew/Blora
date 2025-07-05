package blora.scheduler

import blora.api.scheduler.QuickSchedulerLib
import blora.plugin.BloraPlugin
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object QuickSchedulerLibWrapper : QuickSchedulerLib {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun <T> runTask(dispatcher: CoroutineDispatcher, task: suspend () -> T): Deferred<T> {
        return scope.async(dispatcher) {
            return@async task()
        }
    }

    override fun getBukkitMainScheduler(): CoroutineDispatcher {
        return BukkitMainScheduler
    }

    override fun getBukkitAsyncScheduler(): CoroutineDispatcher {
        return BukkitAsyncScheduler
    }

}

object BukkitMainScheduler : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTask(BloraPlugin, block)
    }

}

object BukkitAsyncScheduler : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BloraPlugin, block)
    }

}