package blora.command.defaults

import blora.internal.api.command.BloraCommandLib
import blora.internal.api.command.literal
import blora.internal.api.command.playerExecutor
import blora.internal.api.command.requires
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