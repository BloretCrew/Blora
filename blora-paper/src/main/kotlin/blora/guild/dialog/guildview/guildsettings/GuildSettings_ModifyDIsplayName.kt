package blora.guild.dialog.guildview.guildsettings

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.input.TextInputControl
import blora.extension.localization
import blora.guild.GuildPermissions
import blora.guild.menu.guildview.guildsettings.guildSettingsMenu
import blora.menu.MenuContext
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component

fun guildSettings_modifyDisplayName(viewer: Player, menuContext: MenuContext, guild: GuildDao, guilds: MutableList<GuildDao>, permissions: Collection<GuildPermissions>, rerenderGuildViewParent: () -> Unit): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogGuild_settingsModify_display_nameTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "guild_display_name",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogGuild_settingsModify_display_nameInputPlaceholderDisplay_name
                    }
                },
                initial = guild.displayName
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
                    DB.trans {
                        guild.displayName = guildDisplayName
                        guild.flush()
                    }
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildUpdateDisplay_name
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