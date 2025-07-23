package blora.guild.dialog.createguild

import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.input.TextInputControl
import blora.extension.localization
import blora.guild.CreatingGuildContext
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import org.bukkit.entity.Player
import plutoproject.adventurekt.component

fun createGuild_modifyDisplayName(viewer: Player, context: CreatingGuildContext, rerenderCallback: () -> Unit): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogCreate_guildModify_display_nameTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "guild_display_name",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogCreate_guildModify_display_nameInputPlaceholderDisplay_name
                    }
                },
                initial = context.displayName
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
                    val guildDisplayName = ((it as NbtCompound)["guild_display_name"] as NbtString).value
                    context.displayName = guildDisplayName
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