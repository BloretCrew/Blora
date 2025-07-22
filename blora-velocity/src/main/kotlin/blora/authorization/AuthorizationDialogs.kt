package blora.authorization

import blora.BloraPlugin
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickType
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.Multiline
import blora.dialog.input.TextInputControl
import blora.extension.localization
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.raw

fun loginDialog(player: Player, warningMessages: Component? = null): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.titleDialogLogin
            }
        },
        body = if (warningMessages != null) {
            listOf(
                PlainMessageDialogBody(
                    contents = component {
                        raw { warningMessages }
                    }
                )
            )
        } else {
            null
        },
        canCloseWithEscape = BloraPlugin.configuration.administration.debug,
        pause = false,
        inputs = listOf(
            TextInputControl(
                key = "blora_password",
                label = component {
                    localization(player) {
                        this.inputDialogLoginPassword
                    }
                },
                maxLength = BloraPlugin.configuration.security.maxPasswordLength,
                multiline = Multiline(
                    maxLines = 1
                )
            )
        ),
        yes = ClickAction(
            label = component {
                localization(player) {
                    this.buttonConfirm
                }
            },
            action = DynamicCustomClickType(
                id = "blora_login",
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.buttonExit
                }
            },
            action = DynamicCustomClickType(
                id = "blora_exit",
            )
        )
    )
}

fun registerDialog(player: Player, warningMessages: Component? = null): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.titleDialogRegister
            }
        },
        body = if (warningMessages != null) {
            listOf(
                PlainMessageDialogBody(
                    contents = component {
                        raw { warningMessages }
                    }
                )
            )
        } else {
            null
        },
        canCloseWithEscape = BloraPlugin.configuration.administration.debug,
        pause = false,
        inputs = listOf(
            TextInputControl(
                key = "blora_password",
                label = component {
                    localization(player) {
                        this.inputDialogRegisterPassword
                    }
                },
                maxLength = BloraPlugin.configuration.security.maxPasswordLength,
                multiline = Multiline(
                    maxLines = 1
                )
            ),
            TextInputControl(
                key = "blora_confirm_password",
                label = component {
                    localization(player) {
                        this.inputDialogRegisterConfirmPassword
                    }
                },
                maxLength = BloraPlugin.configuration.security.maxPasswordLength,
                multiline = Multiline(
                    maxLines = 1
                )
            )
        ),
        yes = ClickAction(
            label = component {
                localization(player) {
                    this.buttonConfirm
                }
            },
            action = DynamicCustomClickType(
                id = "blora_register",
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.buttonExit
                }
            },
            action = DynamicCustomClickType(
                id = "blora_exit",
            )
        )
    )
}