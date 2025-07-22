package blora.guild.dialog.guildview.guildinvitationcode

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildInviteCodeDao
import blora.database.redeem.dao.RedeemDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.BooleanInputControl
import blora.dialog.input.NumberRangeInputControl
import blora.extension.containsLetterAndNumberOnly
import blora.extension.localization
import blora.extension.openDialog
import blora.mail.Attachment
import blora.plugin.BloraPlugin
import blora.redeem.createRedeemDialog
import blora.util.randomString
import net.benwoodworth.knbt.NbtByte
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtFloat
import net.benwoodworth.knbt.NbtString
import org.bukkit.entity.Player
import plutoproject.adventurekt.component
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.sin

fun guildInvitationCode_CreateInvitationCodeDialog(viewer: Player, guild: GuildDao, callback: (String) -> Unit): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogGuild_role_managementCreate_roleTitle
            }
        },
        body = listOf(
            PlainMessageDialogBody(
                contents = component {
                    localization(viewer) {
                        this.guild.dialog.dialogCreate_invitation_codeDescription
                    }
                }
            )
        ),
        inputs = listOf(
            BooleanInputControl(
                key = "single_use",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogCreate_invitation_codeInputSingle_use
                    }
                }
            ),
            BooleanInputControl(
                key = "infinite",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogCreate_invitation_codeInputInfinite
                    }
                }
            ),
            NumberRangeInputControl(
                key = "expire_days",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogCreate_invitation_codeInputExpire_days
                    }
                },
                start = 1f,
                end = 30f,
                initial = 1f,
                step = 1f
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
                    val singleUse = (compound["single_use"] as NbtByte).value == 1.toByte()
                    val infinite = (compound["infinite"] as NbtByte).value == 1.toByte()
                    val expireDays = (compound["expire_days"] as NbtFloat).value.toInt()

                    val code = randomString(8)
                    DB.trans {
                        GuildInviteCodeDao.new {
                            this.guildId = guild.gid
                            this.inviteCode = code
                            this.singleUsable = singleUse
                            this.expireAt = if (infinite) null else LocalDate.now().plusDays(expireDays.toLong())

                            this.creator = viewer.uniqueId
                            this.createdAt = LocalDateTime.now()
                        }
                    }
                    callback(code)
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