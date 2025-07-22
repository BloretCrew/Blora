package blora.command.defaults

import blora.guild.menu.guildMenu
import blora.internal.api.command.BloraCommandLib
import blora.internal.api.command.playerExecutor
import blora.internal.api.command.requires
import blora.permission.Permissions

object GuildCommand {

    fun register() {
        BloraCommandLib.registerCommand("guild") {
            requires {
                this.hasPermission(Permissions.Commands.Guild)
            }

            playerExecutor {
                guildMenu(this.player.asBukkit).open()
            }
        }
    }

}