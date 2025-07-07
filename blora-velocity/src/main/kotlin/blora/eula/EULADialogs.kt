package blora.eula

import blora.BloraPlugin
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.CustomClickType
import blora.dialog.body.PlainMessageDialogBody
import blora.extension.localization
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini

fun eulaDialog(player: Player): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.titleDialogEula
            }
        },
        canCloseWithEscape = BloraPlugin.configuration.administration.debug,
        pause = false,
        body = listOf(
            PlainMessageDialogBody(
                contents = component {
                    mini(EULA.loadEULA())
                }
            )
        ),
        yes = ClickAction(
            label = component {
                localization(player) {
                    this.buttonDialogEulaAccept
                }
            },
            action = CustomClickType(
                id = "blora_eula_accept"
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.buttonDialogEulaReject
                }
            },
            action = CustomClickType(
                id = "blora_eula_reject"
            )
        )
    )
}
