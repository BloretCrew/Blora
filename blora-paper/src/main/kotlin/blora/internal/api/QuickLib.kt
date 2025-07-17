package blora.internal.api

import blora.internal.api.command.BloraCommandLib
import blora.internal.api.scheduler.QuickSchedulerLib
import blora.plugin.BloraPlugin

interface QuickLib {

    fun getCommandLib(): BloraCommandLib

    fun getSchedulerLib(): QuickSchedulerLib

    fun getEntityLib(): QuickEntityLib

    companion object : QuickLib by BloraPlugin

}