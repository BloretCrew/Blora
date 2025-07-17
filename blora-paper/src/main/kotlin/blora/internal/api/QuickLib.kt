package blora.internal.api

import blora.api.command.BloraCommandLib
import blora.api.scheduler.QuickSchedulerLib
import blora.plugin.BloraPlugin

interface QuickLib {

    fun getCommandLib(): BloraCommandLib

    fun getSchedulerLib(): QuickSchedulerLib

    fun getEntityLib(): QuickEntityLib

    companion object : QuickLib by BloraPlugin

}