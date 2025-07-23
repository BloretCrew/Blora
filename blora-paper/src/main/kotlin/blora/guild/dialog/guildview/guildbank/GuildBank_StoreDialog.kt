package blora.guild.dialog.guildview.guildbank

import blora.database.DB
import blora.database.guild.dao.GuildBankLogDao
import blora.database.guild.dao.GuildDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.NumberRangeInputControl
import blora.extension.localization
import blora.extension.openDialog
import blora.plugin.ThirdPartys
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtFloat
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDateTime


fun guildBank_storeDialog(
    viewer: Player,
    guild: GuildDao,
    callback: () -> Unit,
    warningMessage: Component? = null,
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogGuild_bankStoreTitle
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
            NumberRangeInputControl(
                key = "amount",
                label = component {
                    localization(viewer) {
                        this.guild.dialog.dialogGuild_bankInput
                    }
                },
                start = 0f,
                end = ThirdPartys.vaultApi.getBalance(viewer).toFloat(),
                initial = 0f,
                step = 0.1f
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
                    val amount = (compound["amount"] as NbtFloat).value
                    if (amount == 0f)
                        return@DynamicCustomClickTypeInjected
                    if (amount > ThirdPartys.vaultApi.getBalance(viewer)) {
                        viewer.openDialog(
                            guildBank_storeDialog(
                                viewer,
                                guild,
                                callback,
                                component {
                                    localization(viewer) {
                                        this.guild.dialog.dialogGuild_bankStoreWarning
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    DB.trans {
                        GuildBankLogDao.new {
                            this.guildId = guild.gid
                            this.store = true
                            this.value = amount.toDouble()
                            this.timestamp = LocalDateTime.now()
                            this.operator = viewer.uniqueId
                        }
                        guild.bankBalance += amount
                        if (guild.bankBalance > guild.bankBalanceMax) {
                            guild.bankBalanceMax = guild.bankBalance
                        }
                        guild.flush()
                    }
                    ThirdPartys.vaultApi.withdrawPlayer(viewer, amount.toDouble())
                    Bukkit.getOnlinePlayers()
                        .filter { player -> guild.members.contains(player.uniqueId) }
                        .forEach { player ->
                            player.send {
                                localization(
                                    player,
                                    tags = {
                                        parsedPlaceholder("player", viewer.name)
                                        parsedPlaceholder("guild", guild.displayName)
                                        parsedPlaceholder("amount", amount.toString())
                                    }
                                ) {
                                    this.guild.guildBankStore
                                }
                            }
                        }
                    callback()
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