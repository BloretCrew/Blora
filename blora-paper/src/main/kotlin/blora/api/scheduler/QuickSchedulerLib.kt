package blora.api.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import blora.api.QuickLib

interface QuickSchedulerLib {

    fun <T> runTask(dispatcher: CoroutineDispatcher, task: suspend () -> T): Deferred<T>

    fun getBukkitMainScheduler(): CoroutineDispatcher

    fun getBukkitAsyncScheduler(): CoroutineDispatcher

    companion object : QuickSchedulerLib by QuickLib.getSchedulerLib()

}