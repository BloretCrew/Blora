package blora.internal.api.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers

fun <T> bukkitTask(dispatcher: CoroutineDispatcher, block: suspend () -> T): Deferred<T> {
    return QuickSchedulerLib.runTask<T>(dispatcher, block)
}

val Dispatchers.BukkitMain: CoroutineDispatcher
    get() = QuickSchedulerLib.getBukkitMainScheduler()

val Dispatchers.BukkitAsync: CoroutineDispatcher
    get() = QuickSchedulerLib.getBukkitAsyncScheduler()