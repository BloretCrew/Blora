package blora.command.defaults

import blora.api.command.BloraCommandLib
import blora.api.command.literal
import blora.api.command.playerExecutor
import blora.api.command.requires
import blora.extension.openDialog
import blora.modules.redeem.redeemDialog
import blora.modules.redeem.redeemManagementMenu
import blora.permission.Permissions

object RedeemCommand {

    fun register() {
        BloraCommandLib.registerCommand("redeem") {
            requires {
                return@requires this.hasPermission(Permissions.Commands.Redeem)
                        || this.hasPermission(Permissions.Admin)
            }

            literal("manage") {
                requires {
                    this.hasPermission(Permissions.Admin)
                }

                playerExecutor {
                    redeemManagementMenu(this.player.asBukkit).open()
                }
            }

            playerExecutor {
                this.player.asBukkit.openDialog(redeemDialog(this.player.asBukkit))
            }
        }
    }

}