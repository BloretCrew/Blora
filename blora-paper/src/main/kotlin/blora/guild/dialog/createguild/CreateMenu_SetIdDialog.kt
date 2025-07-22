package blora.guild.dialog.createguild

import blora.configuration.CONF
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.TextInputControl
import blora.extension.containsLetterAndNumberOnly
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.CreatingGuildContext
import blora.guild.menu.createguild.createMenuPage
import blora.menu.MenuContext
import blora.plugin.BloraPlugin
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

fun createGuild_setIdDialog(viewer: Player, menuContext: MenuContext, context: CreatingGuildContext, warningMessage: Component? = null): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogCreate_guildSet_idTitle
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
                        this.guild.dialog.dialogCreate_guildSet_idInputPlaceholderId
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
                            createGuild_setIdDialog(
                                viewer,
                                menuContext,
                                context,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogCreate_guildSet_idWarningId_illegal
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (guildId.length < CONF.guild.minIdLength || guildId.length > CONF.guild.maxIdLength) {
                        viewer.openDialog(
                            createGuild_setIdDialog(
                                viewer,
                                menuContext,
                                context,
                                component {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("min", CONF.guild.minIdLength.toString())
                                            parsedPlaceholder("max", CONF.guild.maxIdLength.toString())
                                        }
                                    ) {
                                        this.guild.dialog.dialogCreate_guildSet_idWarningId_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (BloraPlugin.database.getGuildByGid(guildId) != null) {
                        viewer.openDialog(
                            createGuild_setIdDialog(
                                viewer,
                                menuContext,
                                context,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogCreate_guildSet_idWarningId_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    context.id = guildId
                    menuContext.stack.replace(createMenuPage(viewer, context))
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