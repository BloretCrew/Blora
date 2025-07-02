package blora.api

import blora.api.command.QuickCommandLib
import blora.api.scheduler.QuickSchedulerLib
import blora.plugin.BloraPlugin

interface QuickLib {

    fun getCommandLib(): QuickCommandLib

    fun getSchedulerLib(): QuickSchedulerLib

    fun getEntityLib(): QuickEntityLib

    companion object : QuickLib by BloraPlugin

}