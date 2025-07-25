package blora.guild.dialog.guildview.guildbank

import blora.configuration.CONF
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
import blora.extension.format
import blora.extension.localization
import blora.extension.openDialog
import blora.formula.FormulaTokenizer
import blora.formula.FormulaVariable
import blora.guild.formula.GuildFormulaVariable
import blora.player.PapiFormulaVariable
import blora.plugin.BloraPlugin
import blora.plugin.ThirdPartys
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtFloat
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.componentPlaceholder
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
                        val playerBalance = GuildPlayerBalanceDao.find {
                            GuildPlayerBalanceTable.gid eq guild.gid and
                                    (GuildPlayerBalanceTable.player eq viewer.uniqueId)
                        }.firstOrNull() ?: GuildPlayerBalanceDao.new {
                            this.guildId = guild.gid
                            this.player = viewer.uniqueId
                            this.contribution = 0.0
                        }

                        val oldContribution = playerBalance.contribution
                        playerBalance.contribution += amount
                        playerBalance.flush()

                        GuildBankLogDao.new {
                            this.guildId = guild.gid
                            this.store = true
                            this.value = amount.toDouble()
                            this.timestamp = LocalDateTime.now()
                            this.operator = viewer.uniqueId
                        }

                        guild.bankBalance += amount

                        val guildVariable = GuildFormulaVariable(guild)

                        if (guild.bankBalance > guild.bankBalanceMax) {
                            val delta = guild.bankBalance - guild.bankBalanceMax
                            guild.bankBalanceMax = guild.bankBalance
                            val formula = FormulaTokenizer.parse(CONF.guild.vitality.bankBalanceNewMaxVitalityFormula)
                            if (formula == null) {
                                BloraPlugin.slF4JLogger.error("公会银行最大值更新计算公式无法正常解析(1)，请调整后重启服务器")
                            } else {
                                val value = formula.calculate(guildVariable, FormulaVariable.simple("delta", delta))
                                if (value == null) {
                                    BloraPlugin.slF4JLogger.error("公会银行最大值更新计算公式无法正常解析(2)，请调整后重启服务器")
                                } else {
                                    guild.vitality += value
                                    guild.flush()
                                    Bukkit.getOnlinePlayers()
                                        .filter { player -> guild.members.contains(player.uniqueId) }
                                        .forEach { player ->
                                            player.send {
                                                localization(
                                                    player = player,
                                                    tags = {
                                                        parsedPlaceholder("guild", guild.displayName)
                                                        parsedPlaceholder("vitality", value.format(2))
                                                        componentPlaceholder("reason") {
                                                            localization(player) {
                                                                this.guild.guildVitalityAddReasonBank_new_max_balance
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    this.guild.guildVitalityAdd
                                                }
                                            }
                                        }
                                }
                            }
                        }
                        guild.flush()

                        if (playerBalance.contribution > 0) {
                            val delta = (if (oldContribution <= 0)
                                amount
                            else
                                amount / 2).toDouble()
                            val formula = FormulaTokenizer.parse(CONF.guild.vitality.playerContributionVitalityFormula)
                            if (formula == null) {
                                BloraPlugin.slF4JLogger.error("玩家银行贡献计算公式无法正常解析(1)，请调整后重启服务器")
                            } else {
                                val papiVariable = PapiFormulaVariable(viewer)
                                val value = formula.calculate(guildVariable, papiVariable, FormulaVariable.simple("delta", delta))
                                if (value == null) {
                                    BloraPlugin.slF4JLogger.error("玩家银行贡献更新计算公式无法正常解析(2)，请调整后重启服务器")
                                } else {
                                    guild.vitality += value
                                    guild.flush()
                                    Bukkit.getOnlinePlayers()
                                        .filter { player -> guild.members.contains(player.uniqueId) }
                                        .forEach { player ->
                                            player.send {
                                                localization(
                                                    player = player,
                                                    tags = {
                                                        parsedPlaceholder("guild", guild.displayName)
                                                        parsedPlaceholder("vitality", value.format(2))
                                                        componentPlaceholder("reason") {
                                                            localization(
                                                                player = player,
                                                                tags = {
                                                                    parsedPlaceholder("player", viewer.name)
                                                                }
                                                            ) {
                                                                this.guild.guildVitalityAddReasonBank_player_contribution
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    this.guild.guildVitalityAdd
                                                }
                                            }
                                        }
                                }
                            }
                        }
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