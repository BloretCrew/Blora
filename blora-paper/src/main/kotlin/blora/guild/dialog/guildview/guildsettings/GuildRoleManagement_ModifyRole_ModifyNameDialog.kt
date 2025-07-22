package blora.guild.dialog.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildRoleDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.input.TextInputControl
import blora.extension.localization
import blora.menu.MenuContext
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildRoleManagement_modifyRole_modifyNameDialog(viewer: Player, role: GuildRoleDao, context: MenuContext): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogGuild_role_managementModify_roleModify_nameTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "role_name",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogGuild_role_managementModify_roleModify_nameInputPlaceholderName
                    }
                },
                initial = role.displayName
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
                    val roleName = ((it as NbtCompound)["role_name"] as NbtString).value
                    DB.trans {
                        role.displayName = roleName
                        role.flush()
                    }
                    viewer.send {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("role", role.displayName)
                            }
                        ) {
                            this.guild.guildRoleUpdate_name
                        }
                    }
                    context.menu.rerender()
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