package blora.message

import blora.api.message.commandPrefix
import blora.api.message.errorMessage
import plutoproject.adventurekt.component

object DefaultMessages {

    object Errors {

        object Commands {

            val MUST_BE_PLAYER = component {
                commandPrefix()
                errorMessage("只有玩家才可以执行该命令")
            }

            val MUST_BE_BLOCK = component {
                commandPrefix()
                errorMessage("只有方块才可以执行该命令")
            }

            val NO_SUITABLE_EXECUTOR = component {
                commandPrefix()
                errorMessage("无法运行该命令")
            }

        }

    }

}