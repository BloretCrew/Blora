package blora.guild

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.table.GuildBloriusToVitalityTimesTable
import blora.extension.format
import blora.extension.localization
import blora.formula.FormulaTokenizer
import blora.guild.formula.GuildFormulaVariable
import blora.internal.api.scheduler.BukkitAsync
import blora.listener.BasicListener
import blora.player.PapiFormulaVariable
import blora.plugin.BloraPlugin
import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.exposed.sql.update
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.parsedPlaceholder
import kotlin.math.max

object GuildVitalityManager {

    private var guildJob: BukkitTask? = null
    private var playerOnlineJob: BukkitTask? = null

    fun startJob() {
        // use blorius to exchange vitality
        BloraPlugin.scope.launch(Dispatchers.BukkitAsync) {
            doInfinity("0 4 * * *") {
                DB.trans {
                    GuildBloriusToVitalityTimesTable.update {
                        it[GuildBloriusToVitalityTimesTable.times] = 0
                    }
                }
            }
        }
        // guild
        Bukkit.getScheduler().runTaskTimer(
            BloraPlugin,
            { task ->
                if (this.guildJob == null) {
                    this.guildJob = task
                }
                val formula = FormulaTokenizer.parse(CONF.guild.vitality.guildVitalityFormula)
                if (formula == null) {
                    BloraPlugin.slF4JLogger.error("公会活跃点计算公式无法正常解析(1)，请调整后重启服务器")
                    return@runTaskTimer
                }
                for (guild in DB.listGuilds()) {
                    val variable = GuildFormulaVariable(guild)
                    val vitalties = formula.calculate(variable)
                    if (vitalties == null) {
                        BloraPlugin.slF4JLogger.error("公会活跃点计算公式无法正常解析(2)，请调整后重启服务器")
                        return@runTaskTimer
                    }
                    DB.trans {
                        guild.vitality += vitalties
                        guild.flush()
                    }
                    Bukkit.getOnlinePlayers()
                        .filter { guild.members.contains(it.uniqueId) }
                        .forEach {
                            it.send {
                                localization(
                                    player = it,
                                    tags = {
                                        parsedPlaceholder("guild", guild.displayName)
                                        parsedPlaceholder("vitality", vitalties.format(2))
                                        componentPlaceholder("reason") {
                                            localization(it) {
                                                this.guild.guildVitalityAddReasonScheduled_task
                                            }
                                        }
                                    }
                                ) {
                                    this.guild.guildVitalityAdd
                                }
                            }
                        }
                }
            },

            max(10, CONF.guild.vitality.guildVitalityFrequency) * 20L * 60L,
            max(10, CONF.guild.vitality.guildVitalityFrequency) * 20L * 60L
        )
        // player online
        Bukkit.getScheduler().runTaskTimer(
            BloraPlugin,
            { task ->
                if (this.playerOnlineJob == null) {
                    this.playerOnlineJob = task
                }
                val formula = FormulaTokenizer.parse(CONF.guild.vitality.playerOnlineVitalityFormula)
                if (formula == null) {
                    BloraPlugin.slF4JLogger.error("玩家在线活跃点计算公式无法正常解析(1)，请调整后重启服务器")
                    return@runTaskTimer
                }
                for (player in Bukkit.getOnlinePlayers()) {
                    val onlineData = DB.getPlayerOnlineData(player.uniqueId)
                    var error = false
                    for (guildOnline in DB.listPlayerOnlines(player.uniqueId)) {
                        val guild = DB.getGuildByGid(guildOnline.guildId)!!
                        val guildVariable = GuildFormulaVariable(guild)
                        val papiVariable = PapiFormulaVariable(player)
                        val (newLast, times) = onlineData.hasTimesOfHoursAfter(
                            guildOnline.lastCalculate,
                            CONF.guild.vitality.playerOnlineDuration,
                            BasicListener.joinAt[player]!!.toJavaLocalDateTime()
                        )
                        if (times == 0)
                            continue
                        val baseValue = formula.calculate(guildVariable, papiVariable)
                        if (baseValue == null) {
                            BloraPlugin.slF4JLogger.error("玩家在线活跃点计算公式无法正常解析(2)，请调整后重启服务器")
                            error = true
                            break
                        }
                        DB.trans {
                            guildOnline.lastCalculate = newLast
                            guildOnline.flush()

                            guild.vitality += baseValue * times
                            guild.flush()
                        }
                        Bukkit.getOnlinePlayers()
                            .filter { guild.members.contains(it.uniqueId) }
                            .forEach {
                                it.send {
                                    localization(
                                        player = it,
                                        tags = {
                                            parsedPlaceholder("guild", guild.displayName)
                                            parsedPlaceholder("vitality", (baseValue * times).format(2))
                                            componentPlaceholder("reason") {
                                                localization(
                                                    player = it,
                                                    tags = {
                                                        parsedPlaceholder("player", player.name)
                                                    }
                                                ) {
                                                    this.guild.guildVitalityAddReasonPlayer_online
                                                }
                                            }
                                        }
                                    ) {
                                        this.guild.guildVitalityAdd
                                    }
                                }
                            }
                    }
                    if (error)
                        break
                }
            },
            1 * 20L * 60L,
            1 * 20L * 60L // 1 mins
        )
    }

    fun stopJob() {
        this.guildJob?.cancel()
        this.playerOnlineJob?.cancel()
    }

}