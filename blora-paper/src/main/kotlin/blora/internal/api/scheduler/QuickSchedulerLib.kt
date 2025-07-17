package blora.internal.api.scheduler

import blora.internal.api.QuickLib
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred

interface QuickSchedulerLib {

    fun <T> runTask(dispatcher: CoroutineDispatcher, task: suspend () -> T): Deferred<T>

    fun getBukkitMainScheduler(): CoroutineDispatcher

    fun getBukkitAsyncScheduler(): CoroutineDispatcher

    companion object : QuickSchedulerLib by QuickLib.getSchedulerLib()

}