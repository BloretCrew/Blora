package blora.modules.redeem

import blora.database.redeem.RedeemDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.BooleanInputControl
import blora.dialog.input.TextInputControl
import blora.extension.containsLetterAndNumberOnly
import blora.extension.localization
import blora.extension.openDialog
import blora.modules.mail.Attachment
import blora.plugin.BloraPlugin
import net.benwoodworth.knbt.NbtByte
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDateTime

fun createRedeemDialog(
    player: Player,
    warningMessage: Component? = null,
    createCallback: (RedeemDao) -> Unit
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.dialogCreate_redeemTitle
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
                key = "redeem_code",
                label = component {
                    localization(player) {
                        this.dialogCreate_redeemInputPlaceholderRedeem
                    }
                }
            ),
            BooleanInputControl(
                key = "one_use",
                label = component {
                    localization(player) {
                        this.dialogCreate_redeemInputPlaceholderOne_use
                    }
                }
            )
        ),
        yes = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonConfirm
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    val compound = it as NbtCompound
                    val code = (compound["redeem_code"] as NbtString).value.lowercase()
                    val oneUse = (compound["one_use"] as NbtByte).value == 1.toByte()
                    if (!code.containsLetterAndNumberOnly()) {
                        player.openDialog(
                            createRedeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.dialogCreate_redeemWarningName_illegal
                                    }
                                },
                                createCallback
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (BloraPlugin.database.isRedeemCodeExists(code)) {
                        player.openDialog(
                            createRedeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.dialogCreate_redeemWarningExists
                                    }
                                },
                                createCallback
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    BloraPlugin.database.trans {
                        val redeem = RedeemDao.new {
                            this.code = code.lowercase()
                            this.parseAttachment = Attachment()
                            this.creator = player.uniqueId
                            this.createdAt = LocalDateTime.now()
                            this.oneUse = oneUse
                        }
                        createCallback(redeem)
                    }
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            }
        )
    )
}

fun redeemDialog(
    player: Player,
    warningMessage: Component? = null,
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.dialogRedeemTitle
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
                key = "redeem_code",
                label = component {
                    localization(player) {
                        this.dialogRedeemInputPlaceholderRedeem
                    }
                }
            )
        ),
        yes = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonConfirm
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    val compound = it as NbtCompound
                    val code = (compound["redeem_code"] as NbtString).value.lowercase()
                    if (!BloraPlugin.database.isRedeemCodeExists(code)) {
                        player.openDialog(
                            redeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.dialogRedeemWarningNot_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (BloraPlugin.database.isRedeemOneUseAndUsed(code)) {
                        player.openDialog(
                            redeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.dialogRedeemWarningUsed
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (BloraPlugin.database.isRedeemCodeUsedForPlayer(player.uniqueId, code)) {
                        player.openDialog(
                            redeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.dialogRedeemWarningRedeemed
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    RedeemModule.redeemCode(player, code)
                    player.send {
                        localization(
                            player = player,
                            tags = {
                                parsedPlaceholder("redeem", code)
                            }
                        ) {
                            this.redeemSuccess
                        }
                    }
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            }
        )
    )
}