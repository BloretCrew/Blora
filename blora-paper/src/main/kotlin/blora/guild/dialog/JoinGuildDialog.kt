package blora.guild.dialog

import blora.database.DB
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.TextInputControl
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.GuildJoinSource
import blora.guild.GuildJoinStrategy
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDate

fun joinGuildDialog(viewer: Player, warningMessage: Component? = null): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogJoin_guildTitle
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
                key = "code",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogJoin_guildInputCode
                    }
                }
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
                    val compound = it as NbtCompound
                    val code = (compound["code"] as NbtString).value

                    val inviteCode = DB.getInvitationCode(code)
                    if (inviteCode == null) {
                        viewer.openDialog(
                            joinGuildDialog(
                                viewer,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogJoin_guildWarningCode_not_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    if (inviteCode.outdated) {
                        viewer.openDialog(
                            joinGuildDialog(
                                viewer,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogJoin_guildWarningCode_not_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    if (inviteCode.expireAt != null && inviteCode.expireAt!! < LocalDate.now()) {
                        DB.trans {
                            inviteCode.outdated = true
                            inviteCode.flush()
                        }
                        viewer.openDialog(
                            joinGuildDialog(
                                viewer,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogJoin_guildWarningCode_not_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    val guild = DB.getGuildByGid(inviteCode.guildId)
                    if (guild == null) {
                        DB.trans {
                            inviteCode.delete()
                        }
                        viewer.openDialog(
                            joinGuildDialog(
                                viewer,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogJoin_guildWarningCode_not_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    if (guild.blocklist.contains(viewer.uniqueId)) {
                        viewer.openDialog(
                            joinGuildDialog(
                                viewer,
                                component {
                                    localization(viewer) {
                                        this.guild.guildBlocked
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    if (guild.members.contains(viewer.uniqueId)) {
                        viewer.openDialog(
                            joinGuildDialog(
                                viewer,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogJoin_guildWarningIs_member
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    if (guild.joinStrategy == GuildJoinStrategy.NOT_ALLOW) {
                        viewer.openDialog(
                            joinGuildDialog(
                                viewer,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogJoin_guildWarningGuild_reject
                                    }
                                }
                            )
                        )

                        return@DynamicCustomClickTypeInjected
                    }

                    if (inviteCode.singleUsable) {
                        DB.trans {
                            inviteCode.outdated = true
                            inviteCode.flush()
                        }
                    }

                    if (guild.joinStrategy == GuildJoinStrategy.REQUIRE_REVIEW) {
                        DB.createJoinRequest(guild, viewer, GuildJoinSource.InvitationCode(code))
                        DB.announceJoinRequest(guild, viewer)
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("guild", guild.displayName)
                                    parsedPlaceholder("code", code)
                                }
                            ) {
                                this.guild.guildInvitation_code_joinAccept
                            }
                        }
                    } else {
                        DB.guildJoinDirect(guild, viewer.uniqueId, GuildJoinSource.InvitationCode(code))
                        DB.announceJoin(guild, viewer)
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("guild", guild.displayName)
                                }
                            ) {
                                this.guild.guildJoinSuccess
                            }
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