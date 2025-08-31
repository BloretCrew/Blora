package blora.guild.dialog.guildview.guildbank

import blora.database.DB
import blora.database.guild.dao.GuildBankLogDao
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildPlayerBalanceDao
import blora.database.guild.table.GuildPlayerBalanceTable
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
import org.jetbrains.exposed.sql.and
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDateTime


fun guildBank_withdrawDialog(
    viewer: Player,
    guild: GuildDao,
    callback: () -> Unit,
    warningMessage: Component? = null,
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.guild.dialog.dialogGuild_bankWithdrawTitle
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
                end = guild.bankBalance.toFloat(),
                initial = 0f,
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
                    val amount = (compound["amount"] as NbtFloat).value
                    if (amount == 0f)
                        return@DynamicCustomClickTypeInjected
                    guild.withLock {
                        DB.trans {
                            guild.refresh()

                            if (amount > guild.bankBalance) {
                                viewer.openDialog(
                                    guildBank_withdrawDialog(
                                        viewer,
                                        guild,
                                        callback,
                                        component {
                                            localization(viewer) {
                                                this.guild.dialog.dialogGuild_bankWithdrawWarning
                                            }
                                        }
                                    )
                                )
                                return@trans
                            }

                            val playerBalance = GuildPlayerBalanceDao.find {
                                GuildPlayerBalanceTable.gid eq guild.gid and
                                        (GuildPlayerBalanceTable.player eq viewer.uniqueId)
                            }.firstOrNull() ?: GuildPlayerBalanceDao.new {
                                this.guildId = guild.gid
                                this.player = viewer.uniqueId
                                this.contribution = 0.0
                            }

                            playerBalance.contribution -= amount * 2
                            playerBalance.flush()

                            GuildBankLogDao.new {
                                this.guildId = guild.gid
                                this.store = false
                                this.value = amount.toDouble()
                                this.timestamp = LocalDateTime.now()
                                this.operator = viewer.uniqueId
                            }
                            guild.bankBalance -= amount
                            guild.flush()

                            guild.refresh()

                            ThirdPartys.vaultApi.depositPlayer(viewer, amount.toDouble())
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
                                            this.guild.guildBankWithdraw
                                        }
                                    }
                                }
                            callback()
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