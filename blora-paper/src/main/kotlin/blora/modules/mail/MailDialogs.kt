package blora.modules.mail

import blora.database.mail.MailDao
import blora.database.mail.SystemMailDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.MultiActionDialog
import blora.dialog.NoticeDialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.Multiline
import blora.dialog.input.NumberRangeInputControl
import blora.dialog.input.TextInputControl
import blora.extension.localization
import blora.extension.openDialog
import blora.menu.Menu
import blora.modules.mail.trigger.OnlineInRangeTrigger
import blora.plugin.BloraPlugin
import blora.util.castString
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtFloat
import net.benwoodworth.knbt.NbtString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.text
import java.time.LocalDate
import java.time.LocalDateTime

fun modifyMailTitleDialog(player: Player, menu: Menu, context: CreatingMailContext): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.dialogModify_titleTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "title",
                label = component {
                    localization(player) {
                        this.dialogModify_titleInputPlaceholderTitle
                    }
                },
                initial = context.title,
                maxLength = 512,
                multiline = Multiline(
                    maxLines = 1
                )
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
                    context.title = (compound["title"] as NbtString).value
                    menu.rerender()
                    (menu.contextObject as CreatingMailContext).modifying = false
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    menu.rerender()
                    (menu.contextObject as CreatingMailContext).modifying = false
                }
            )
        )
    )
}

fun modifyMailSystemSenderDialog(player: Player, menu: Menu, context: CreatingMailContext): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.dialogModify_system_senderTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "system_sender",
                label = component {
                    localization(player) {
                        this.dialogModify_system_senderInputPlaceholderSystem_sender
                    }
                },
                initial = context.sender,
                maxLength = 512,
                multiline = Multiline(
                    maxLines = 1
                )
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
                    context.sender = (compound["system_sender"] as NbtString).value
                    menu.rerender()
                    (menu.contextObject as CreatingMailContext).modifying = false
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    menu.rerender()
                    (menu.contextObject as CreatingMailContext).modifying = false
                }
            )
        )
    )
}

fun modifyMailContentsDialog(player: Player, menu: Menu, context: CreatingMailContext): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.dialogModify_contentsTitle
            }
        },
        inputs = listOf(
            TextInputControl(
                key = "contents",
                label = component {
                    localization(player) {
                        this.dialogModify_contentsInputPlaceholderContents
                    }
                },
                initial = context.contents,
                maxLength = 2048,
                multiline = Multiline(
                    maxLines = 1024,
                    height = 200
                )
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
                    context.contents = (compound["contents"] as NbtString).value
                    menu.rerender()
                    (menu.contextObject as CreatingMailContext).modifying = false
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    menu.rerender()
                    (menu.contextObject as CreatingMailContext).modifying = false
                }
            )
        )
    )
}

fun modifyDateDialog(
    player: Player,
    warningMessage: Component?,
    title: Component,
    yesCallback: (LocalDate) -> Unit,
    noCallback: () -> Unit
): Dialog {
    return ConfirmationDialog(
        title = title,
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
                key = "year",
                label = component {
                    localization(player) {
                        this.dialogModify_dateInputPlaceholderYear
                    }
                }
            ),
            NumberRangeInputControl(
                key = "month",
                label = component {
                    localization(player) {
                        this.dialogModify_dateInputPlaceholderMonth
                    }
                },
                start = 1f,
                end = 12f,
                step = 1f,
                initial = 1f
            ),
            NumberRangeInputControl(
                key = "day",
                label = component {
                    localization(player) {
                        this.dialogModify_dateInputPlaceholderDay
                    }
                },
                start = 1f,
                end = 31f,
                step = 1f,
                initial = 1f
            ),
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
                    val year = (compound["year"] as NbtString).value.toIntOrNull()
                    if (year == null) {
                        player.openDialog(
                            modifyDateDialog(player, component {
                                localization(player) {
                                    this.dialogModify_dateWarningYear_must_be_no_negative_digital
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (year <= 0) {
                        player.openDialog(
                            modifyDateDialog(player, component {
                                localization(player) {
                                    this.dialogModify_dateWarningYear_must_be_no_negative_digital
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    val month = (compound["month"] as NbtFloat).value.toInt()
                    val day = (compound["day"] as NbtFloat).value.toInt()

                    val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
                    val maxDays = when (month) {
                        4, 6, 9, 11 -> 30
                        2 -> if (isLeap) 29 else 28
                        else -> 31
                    }

                    if (day > maxDays) {
                        player.openDialog(
                            modifyDateDialog(player, component {
                                localization(
                                    player = player,
                                    tags = {
                                        parsedPlaceholder("year", year.toString())
                                        parsedPlaceholder("month", month.toString())
                                    }
                                ) {
                                    this.dialogModify_dateWarningDay_out_of_limit
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    yesCallback(LocalDate.of(year, month, day))
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    noCallback()
                }
            )
        )
    )
}

fun modifyDateTimeDialog(
    player: Player,
    warningMessage: Component?,
    title: Component,
    yesCallback: (LocalDateTime) -> Unit,
    noCallback: () -> Unit
): Dialog {
    return ConfirmationDialog(
        title = title,
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
                key = "year",
                label = component {
                    localization(player) {
                        this.dialogModify_dateInputPlaceholderYear
                    }
                }
            ),
            NumberRangeInputControl(
                key = "month",
                label = component {
                    localization(player) {
                        this.dialogModify_dateInputPlaceholderMonth
                    }
                },
                start = 1f,
                end = 12f,
                step = 1f,
                initial = 1f
            ),
            NumberRangeInputControl(
                key = "day",
                label = component {
                    localization(player) {
                        this.dialogModify_dateInputPlaceholderDay
                    }
                },
                start = 1f,
                end = 31f,
                step = 1f,
                initial = 1f
            ),
            NumberRangeInputControl(
                key = "hour",
                label = component {
                    localization(player) {
                        this.dialogModify_datetimeInputPlaceholderHour
                    }
                },
                start = 0f,
                end = 23f,
                step = 1f,
                initial = 1f
            ),
            NumberRangeInputControl(
                key = "minute",
                label = component {
                    localization(player) {
                        this.dialogModify_datetimeInputPlaceholderMinute
                    }
                },
                start = 0f,
                end = 59f,
                step = 1f,
                initial = 1f
            ),
            NumberRangeInputControl(
                key = "second",
                label = component {
                    localization(player) {
                        this.dialogModify_datetimeInputPlaceholderSecond
                    }
                },
                start = 0f,
                end = 59f,
                step = 1f,
                initial = 1f
            ),
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
                    val year = (compound["year"] as NbtString).value.toIntOrNull()
                    if (year == null) {
                        player.openDialog(
                            modifyDateTimeDialog(player, component {
                                localization(player) {
                                    this.dialogModify_dateWarningYear_must_be_no_negative_digital
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (year <= 0) {
                        player.openDialog(
                            modifyDateTimeDialog(player, component {
                                localization(player) {
                                    this.dialogModify_dateWarningYear_must_be_no_negative_digital
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    val month = (compound["month"] as NbtFloat).value.toInt()
                    val day = (compound["day"] as NbtFloat).value.toInt()

                    val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
                    val maxDays = when (month) {
                        4, 6, 9, 11 -> 30
                        2 -> if (isLeap) 29 else 28
                        else -> 31
                    }

                    if (day > maxDays) {
                        player.openDialog(
                            modifyDateTimeDialog(player, component {
                                localization(
                                    player = player,
                                    tags = {
                                        parsedPlaceholder("year", year.toString())
                                        parsedPlaceholder("month", month.toString())
                                    }
                                ) {
                                    this.dialogModify_dateWarningDay_out_of_limit
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    val hour = (compound["hour"] as NbtFloat).value.toInt()
                    val minute = (compound["minute"] as NbtFloat).value.toInt()
                    val second = (compound["second"] as NbtFloat).value.toInt()

                    yesCallback(LocalDateTime.of(year, month, day, hour, minute, second))
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    noCallback()
                }
            )
        )
    )
}

fun modifyOnlineInRangeDialog(
    trigger: OnlineInRangeTrigger,
    from: Boolean,
    player: Player,
    warningMessage: Component?,
    title: Component,
    yesCallback: (LocalDate) -> Unit,
    noCallback: () -> Unit
): Dialog {
    return modifyDateDialog(
        player,
        warningMessage,
        component {
            localization(player) {
                this.dialogModify_dateModify_online_before_triggerTitle
            }
        },
        yesCallback = { date ->
            if (from) {
                if (date > trigger.to) {
                    player.openDialog(
                        modifyOnlineInRangeDialog(
                            trigger,
                            true,
                            player,
                            component {
                                localization(player) {
                                    this.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_rangeWarningFrom_later_than_to
                                }
                            },
                            title,
                            yesCallback,
                            noCallback
                        )
                    )
                    return@modifyDateDialog
                }
            } else {
                if (date < trigger.to) {
                    player.openDialog(
                        modifyOnlineInRangeDialog(
                            trigger,
                            false,
                            player,
                            component {
                                localization(player) {
                                    this.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_rangeWarningFrom_later_than_to
                                }
                            },
                            title,
                            yesCallback,
                            noCallback
                        )
                    )
                    return@modifyDateDialog
                }
            }
            yesCallback(date)
        },
        noCallback = noCallback
    )
}

fun modifyDaysDialog(
    player: Player,
    warningMessage: Component?,
    title: Component,
    yesCallback: (Int) -> Unit,
    noCallback: () -> Unit
): Dialog {
    return ConfirmationDialog(
        title = title,
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
                key = "days",
                label = component {
                    localization(player) {
                        this.dialogModify_daysInputPlaceholderDays
                    }
                }
            ),
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
                    val days = (compound["days"] as NbtString).value.toIntOrNull()
                    if (days == null) {
                        player.openDialog(
                            modifyDaysDialog(player, component {
                                localization(player) {
                                    this.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_recent_daysWarningDays_must_be_non_negative
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (days < 0) {
                        player.openDialog(
                            modifyDaysDialog(player, component {
                                localization(player) {
                                    this.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_recent_daysWarningDays_must_be_non_negative
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    yesCallback(days)
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    noCallback()
                }
            )
        )
    )
}

fun modifyAmountDialog(
    player: Player,
    warningMessage: Component?,
    title: Component,
    yesCallback: (UInt) -> Unit,
    noCallback: () -> Unit
): Dialog {
    return ConfirmationDialog(
        title = title,
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
                key = "amount",
                label = component {
                    localization(player) {
                        this.dialogModify_amountInputPlaceholderAmount
                    }
                }
            ),
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
                    val amount = (compound["amount"] as NbtString).value.toUIntOrNull()
                    if (amount == null) {
                        player.openDialog(
                            modifyAmountDialog(player, component {
                                localization(player) {
                                    this.dialogModify_amountWarningMust_be_uint
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (amount == 0u) {
                        player.openDialog(
                            modifyAmountDialog(player, component {
                                localization(player) {
                                    this.dialogModify_amountWarningMust_be_uint
                                }
                            }, title, yesCallback, noCallback)
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    yesCallback(amount)
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(player) {
                    this.dialogButtonCancel
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    noCallback()
                }
            )
        )
    )
}

fun setSystemMailIdentifierDialog(
    context: CreatingMailContext,
    player: Player,
    warningMessage: Component? = null
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(player) {
                this.dialogSet_system_mail_identifierTitle
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
                key = "system_mail_identifier",
                label = component {
                    localization(player) {
                        this.dialogSet_system_mail_identifierInputPlaceholderIdentifier
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
                    val id = (compound["system_mail_identifier"] as NbtString).value
                    if (!id.matches("[a-zA-Z][a-zA-Z0-9_]*".toRegex())) {
                        player.openDialog(
                            setSystemMailIdentifierDialog(
                                context,
                                player,
                                component {
                                    localization(player) {
                                        this.dialogSet_system_mail_identifierWarningIdentifier_illegal
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (BloraPlugin.database.isSystemMailIdUsed(id)) {
                        player.openDialog(
                            setSystemMailIdentifierDialog(
                                context,
                                player,
                                component {
                                    localization(player) {
                                        this.dialogSet_system_mail_identifierWarningIdentifier_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }

                    BloraPlugin.database.saveSystemMailWithContext(player.uniqueId, id, context)
                    player.send {
                        localization(
                            player = player,
                            tags = {
                                parsedPlaceholder("mail_id", id)
                            }
                        ) {
                            this.mailSuccessCreate_system_mail
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

fun systemMailPreview(player: Player, mail: SystemMailDao): Dialog {
    return NoticeDialog(
        title = component {
            localization(player) {
                mail.title
            }
        },
        body = listOf(
            PlainMessageDialogBody(
                contents = component {
                    localization(
                        player = player,
                        tags = {
                            componentPlaceholder("sender") {
                                localization(player) {
                                    if (mail.sender != null) {
                                        mail.sender!!
                                    } else {
                                        this.mailSenderSystemDefault
                                    }
                                }
                            }
                        }
                    ) {
                        this.mailViewSender
                    }
                }
            ),
            PlainMessageDialogBody(
                contents = component {
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("date", mail.sendingDate?.castString() ?: "")
                        }
                    ) {
                        this.mailViewTime
                    }
                }
            ),
            PlainMessageDialogBody(
                contents = component {
                    localization(player) {
                        this.mailViewContents
                    }
                    newline()
                    localization(player) {
                        mail.contents
                    }
                }
            ),
            PlainMessageDialogBody(
                contents = mail.parsedAttachment.buildMailComponent()
            )
        )
    )
}

fun playerViewMail(player: Player, mail: MailDao): Dialog {
    return MultiActionDialog(
        title = component {
            localization(player) {
                mail.title
            }
        },
        body = listOf(
            PlainMessageDialogBody(
                contents = component {
                    localization(
                        player = player,
                        tags = {
                            componentPlaceholder("sender") {
                                val sender = mail.parsedSender
                                when (sender) {
                                    is Sender.Player -> {
                                        text {
                                            BloraPlugin.database.getPlayerDisplayName(sender.uuid)
                                        }
                                    }

                                    is Sender.System -> {
                                        localization(player) {
                                            sender.name.ifEmpty {
                                                this.mailSenderSystemDefault
                                            }
                                        }
                                    }

                                    else -> {
                                        localization(player) {
                                            this.mailSenderUnknown
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        this.mailViewSender
                    }
                }
            ),
            PlainMessageDialogBody(
                contents = component {
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("date", mail.createdAt.castString())
                        }
                    ) {
                        this.mailViewTime
                    }
                }
            ),
            PlainMessageDialogBody(
                contents = component {
                    localization(player) {
                        this.mailViewContents
                    }
                    newline()
                    localization(player) {
                        mail.contents
                    }
                }
            ),
            PlainMessageDialogBody(
                contents = mail.parsedAttachment.buildMailComponent()
            )
        ),
        actions = buildList {
            if (!mail.parsedAttachment.hasNoContent() && !mail.isClaim) {
                this.add(
                    ClickAction(
                        label = component {
                            localization(player) {
                                this.menuMail_listButtonClaim
                            }
                        },
                        action = DynamicCustomClickTypeInjected(
                            callback = {
                                BloraPlugin.database.trans {
                                    mail.isClaim = true
                                    mail.flush()
                                }
                                mail.parsedAttachment.claimToPlayer(player)
                                player.send {
                                    localization(player) {
                                        this.mailClaim_attachment
                                    }
                                }
                            }
                        )
                    )
                )
            }
            this.add(
                ClickAction(
                    label = component {
                        localization(player) {
                            this.menuMail_listButtonBack
                        }
                    }
                )
            )
        },
        columns = 2
    )
}