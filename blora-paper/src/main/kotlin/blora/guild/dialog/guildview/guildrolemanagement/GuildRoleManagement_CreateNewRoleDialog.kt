package blora.guild.dialog.guildview.guildrolemanagement

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildRoleDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.TextInputControl
import blora.extension.containsLetterAndNumberOnly
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.role.RolePermissions
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildRoleManagemenet_createNewRoleDialog(
    viewer: Player,
    guild: GuildDao,
    callback: (GuildRoleDao) -> Unit,
    initialRoleId: String = "",
    initialRoleName: String = "",
    warningMessage: Component? = null,
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogGuild_role_managementCreate_roleTitle
            }
        },
        body = if (warningMessage != null)
            listOf(
                PlainMessageDialogBody(
                    contents = warningMessage
                )
            )
        else
            null,
        inputs = listOf(
            TextInputControl(
                key = "role_id",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogGuild_role_managementCreate_roleInputPlaceholderId
                    }
                },
                initial = initialRoleId,
            ),
            TextInputControl(
                key = "role_name",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogGuild_role_managementCreate_roleInputPlaceholderName
                    }
                },
                initial = initialRoleName,
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
                    DB.trans {
                        guild.refresh()
                    }
                    if (guild.owner != viewer.uniqueId) {
                        return@DynamicCustomClickTypeInjected
                    }
                    val roleId = ((it as NbtCompound)["role_id"] as NbtString).value
                    val roleName = (it["role_name"] as NbtString).value
                    if (!roleId.containsLetterAndNumberOnly() || roleId.isEmpty() || roleId.isBlank()) {
                        viewer.openDialog(
                            guildRoleManagemenet_createNewRoleDialog(
                                viewer,
                                guild,
                                callback,
                                roleId,
                                roleName,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogGuild_role_managementCreate_roleWarningChars_illegal
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (DB.getRoleForGuild(guild.gid, roleId) != null) {
                        viewer.openDialog(
                            guildRoleManagemenet_createNewRoleDialog(
                                viewer,
                                guild,
                                callback,
                                roleId,
                                roleName,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogGuild_role_managementCreate_roleWarningExists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    callback(
                        DB.trans {
                            GuildRoleDao.new {
                                this.roleId = roleId
                                this.displayName = roleName.ifBlank { "" }.ifEmpty { roleId }
                                this.guildId = guild.gid
                                this.systemCreated = false
                                this.creator = viewer.uniqueId
                                this.priority = 2
                                this.permission = RolePermissions()
                                this.ownedMembers = emptyList()
                            }
                        }
                    )
                    viewer.send {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("role", roleName)
                            }
                        ) {
                            this.guild.guildRoleCreate_success
                        }
                    }
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