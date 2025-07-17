package blora.options

import blora.BloraPlugin
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickType
import blora.dialog.input.InputControlOption
import blora.dialog.input.SingleOptionInputControl
import blora.extension.localization
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.component

fun optionsDialog(player: Player): Dialog {
    val options = BloraPlugin.database.getPlayerByName(player.username)!!.jsonOptions
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.titleDialogPlayer_options
            }
        },
        externalTitle = component {
            localization(player) {
                this.titleExternalDialogPlayer_options
            }
        },
        inputs = listOf(
            SingleOptionInputControl(
                key = "blora_playerOptions_alwaysLobby",
                label = component {
                    localization(player) {
                        this.optionsAlways_lobby
                    }
                },
                options = listOf(
                    InputControlOption(
                        id = "not_set",
                        display = component {
                            localization(player) {
                                this.optionDefault
                            }
                        },
                        initial = options.alwaysLobby == OptionStatus.NOT_SET
                    ),
                    InputControlOption(
                        id = "enable",
                        display = component {
                            localization(player) {
                                this.optionEnable
                            }
                        },
                        initial = options.alwaysLobby == OptionStatus.ENABLE
                    ),
                    InputControlOption(
                        id = "disable",
                        display = component {
                            localization(player) {
                                this.optionDisable
                            }
                        },
                        initial = options.alwaysLobby == OptionStatus.DISABLE
                    )
                )
            )
        ),
        canCloseWithEscape = true,
        yes = ClickAction(
            label = component {
                localization(player) {
                    this.buttonSave
                }
            },
            action = DynamicCustomClickType(
                id = "blora_player_options_exit"
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.buttonCancel
                }
            }
        )
    )
}
