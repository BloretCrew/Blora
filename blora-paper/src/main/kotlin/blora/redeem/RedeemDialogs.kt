package blora.redeem

import blora.database.DB
import blora.database.redeem.dao.RedeemDao
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
import blora.mail.Attachment
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
    rerenderCallback: () -> Unit
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.redeem.dialogCreate_redeemTitle
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
                        this.redeem.dialogCreate_redeemInputPlaceholderRedeem
                    }
                }
            ),
            BooleanInputControl(
                key = "one_use",
                label = component {
                    localization(player) {
                        this.redeem.dialogCreate_redeemInputPlaceholderOne_use
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
                                        this.redeem.dialogCreate_redeemWarningName_illegal
                                    }
                                },
                                rerenderCallback
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (DB.isRedeemCodeExists(code)) {
                        player.openDialog(
                            createRedeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.redeem.dialogCreate_redeemWarningExists
                                    }
                                },
                                rerenderCallback
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    val redeem = DB.trans {
                        RedeemDao.new {
                            this.code = code.lowercase()
                            this.parseAttachment = Attachment()
                            this.creator = player.uniqueId
                            this.createdAt = LocalDateTime.now()
                            this.oneUse = oneUse
                        }
                    }

                    player.send {
                        localization(
                            player = player,
                            tags = {
                                parsedPlaceholder("redeem", redeem.code)
                            }
                        ) {
                            this.redeem.redeemCreate
                        }
                    }

                    rerenderCallback()
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
                this.redeem.dialogRedeemTitle
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
                        this.redeem.dialogRedeemInputPlaceholderRedeem
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
                    if (!DB.isRedeemCodeExists(code)) {
                        player.openDialog(
                            redeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.redeem.dialogRedeemWarningNot_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (DB.isRedeemOneUseAndUsed(code)) {
                        player.openDialog(
                            redeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.redeem.dialogRedeemWarningUsed
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (DB.isRedeemCodeUsedForPlayer(player.uniqueId, code)) {
                        player.openDialog(
                            redeemDialog(
                                player,
                                component {
                                    localization(player) {
                                        this.redeem.dialogRedeemWarningRedeemed
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
                            this.redeem.redeemSuccess
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