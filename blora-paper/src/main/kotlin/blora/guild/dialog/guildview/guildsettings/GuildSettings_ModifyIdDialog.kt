package blora.guild.dialog.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.TextInputControl
import blora.extension.containsLetterAndNumberOnly
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.GuildPermissions
import blora.guild.menu.guildview.guildsettings.guildSettingsMenu
import blora.menu.MenuContext
import blora.plugin.BloraPlugin
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component

fun guildSettings_modifyIdDialog(viewer: Player, menuContext: MenuContext, guild: GuildDao, guilds: MutableList<GuildDao>, permissions: Collection<GuildPermissions>, rerenderGuildViewParent: () -> Unit, warningMessage: Component? = null): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogGuild_settingsModify_idTitle
            }
        },
        body = if (warningMessage != null)
            listOf(
                PlainMessageDialogBody(
                    contents = warningMessage
                )
            )
        else
            emptyList(),
        inputs = listOf(
            TextInputControl(
                key = "guild_id",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogGuild_settingsModify_idInputPlaceholderId
                    }
                },

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
                    val guildId = ((it as NbtCompound)["guild_id"] as NbtString).value
                    if (!guildId.containsLetterAndNumberOnly()) {
                        viewer.openDialog(
                            guildSettings_modifyIdDialog(
                                viewer,
                                menuContext,
                                guild,
                                guilds,
                                permissions,
                                rerenderGuildViewParent,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogGuild_settingsSet_idWarningId_illegal
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (BloraPlugin.database.getGuildByGid(guildId) != null) {
                        viewer.openDialog(
                            guildSettings_modifyIdDialog(
                                viewer,
                                menuContext,
                                guild,
                                guilds,
                                permissions,
                                rerenderGuildViewParent,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogGuild_settingsSet_idWarningId_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    DB.updateGuildId(guild, guildId)
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildUpdateId
                        }
                    }
                    menuContext.stack.replace(guildSettingsMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent))
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