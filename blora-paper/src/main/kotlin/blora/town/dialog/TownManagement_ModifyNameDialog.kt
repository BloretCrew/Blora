package blora.town.dialog

import blora.database.DB
import blora.database.town.dao.TownDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.input.TextInputControl
import blora.extension.localization
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component

fun townManagement_modifyNameDialog(
    viewer: Player,
    town: TownDao,
    initialName: String = town.displayName,
    rerenderCallback: () -> Unit
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.town.dialog.town_managementModify_town_nameTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "town_name",
                label = component {
                    localization(viewer) {
                        this.town.dialog.town_managementModify_town_nameInputPlaceholderName
                    }
                },
                initial = initialName
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
                    val townName = ((it as NbtCompound)["town_name"] as NbtString).value
                        .ifBlank { return@DynamicCustomClickTypeInjected }
                        .ifEmpty { return@DynamicCustomClickTypeInjected }
                    DB.trans {
                        town.displayName = townName
                        town.flush()
                    }
                    viewer.send {
                        localization(viewer) {
                            this.town.nameUpdate
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