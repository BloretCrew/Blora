package blora.town.dialog

import blora.database.DB
import blora.database.town.dao.TownDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.input.Multiline
import blora.dialog.input.TextInputControl
import blora.extension.localization
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component

fun townManagement_modifyWelcomeMessageDialog(
    viewer: Player,
    town: TownDao,
    initialMessage: String = town.welcomeMessage ?: "",
    rerenderCallback: () -> Unit
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.town.dialog.town_managementModify_welcome_messageTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "welcome_message",
                label = component {
                    localization(viewer) {
                        this.town.dialog.town_managementModify_welcome_messageInputPlaceholderName
                    }
                },
                initial = initialMessage,
                maxLength = 1024,
                multiline = Multiline(
                    maxLines = 5,
                    height = 100
                )
            )
        ),
        yes = ClickAction(
            label = component {
                localization(viewer) {
                    this.dialogButtonConfirm
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    val welcomeMessage: String? = ((it as NbtCompound)["welcome_message"] as NbtString).value
                        .ifBlank { "" }
                        .ifEmpty { null }
                    DB.trans {
                        town.welcomeMessage = welcomeMessage
                        town.flush()
                    }
                    viewer.send {
                        localization(viewer) {
                            this.town.welcome_messageUpdate
                        }
                    }
                    rerenderCallback()
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(viewer) {
                    this.dialogButtonCancel
                }
            }
        )
    )
}