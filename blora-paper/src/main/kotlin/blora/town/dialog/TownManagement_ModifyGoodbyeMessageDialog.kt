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

fun townManagement_modifyGoodbyeMessageDialog(
    viewer: Player,
    town: TownDao,
    initialMessage: String = town.goodbyeMessage ?: "",
    rerenderCallback: () -> Unit
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.town.dialog.town_managementModify_goodbye_messageTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "goodbye_message",
                label = component {
                    localization(viewer) {
                        this.town.dialog.town_managementModify_goodbye_messageInputPlaceholderName
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
                    val goodbyeMessage: String? = ((it as NbtCompound)["goodbye_message"] as NbtString).value
                        .ifBlank { "" }
                        .ifEmpty { null }
                    DB.trans {
                        town.goodbyeMessage = goodbyeMessage
                        town.flush()
                    }
                    viewer.send {
                        localization(viewer) {
                            this.town.goodbye_messageUpdate
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