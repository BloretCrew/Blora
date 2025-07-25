package blora.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationContents(
    val guild: Guild = Guild(),
    val chat: Chat = Chat(),
    val mail: Mail = Mail(),
    val messaging: Messaging = Messaging(),
    val security: Security = Security(),
    val modules: Modules = Modules(),
    val database: Database = Database(),
)

@Serializable
data class Guild(
    val level: GuildLevel = GuildLevel(),
    val vitality: GuildVitality = GuildVitality(),
    val notifyDelaySeconds: Int = 2,
    val ownerTransferCooldownDays: Int = 30,
    val playerMaxJoin: Int = 3,
    val playerMaxOwn: Int = 1,
    val minIdLength: Int = 2,
    val maxIdLength: Int = 5,
    val createCost: Int = 500
)

@Serializable
data class GuildVitality(
    val bloriusExchangeMaxTimes: Int = 6,
    val bloriusExchangePrice: Int = 100,
    val bloriusExchangeValue: Double = 20.0,
    val guildVitalityFrequency: Int = 30, // unit: minutes
    val guildVitalityFormula: String = "guild:balance * 0.05 + guild:members * 10 + guild:level * 10",
    val playerOnlineDuration: Int = 4, // unit: hours
    val playerOnlineVitalityFormula: String = "50 + papi:player_level * 2",
    val newPlayerJoinVitalityFormula: String = "100",
    val bankBalanceNewMaxVitalityFormula: String = "delta * 0.05",
    val playerContributionVitalityFormula: String = "delta * 0.05",
)

@Serializable
data class GuildLevel(
    val basic: GuildLevelBasic = GuildLevelBasic(),
    val overrides: Map<Int, GuildLevelIncrementOverride> = mapOf(
        3 to GuildLevelIncrementOverride(
            member = 20
        ),
        5 to GuildLevelIncrementOverride(
            member = 30,
            claim = 50,
            upgradeCost = 100
        )
    )
)

@Serializable
data class GuildLevelBasic(
    val member: GuildLevelMember = GuildLevelMember(),
    val claim: GuildLevelClaim = GuildLevelClaim(),
    val upgradeCost: GuildLevelUpgradeCost = GuildLevelUpgradeCost()
)

@Serializable
data class GuildLevelIncrementOverride(
    val member: Int? = null,
    val claim: Int? = null,
    val upgradeCost: Int? = null,
)

@Serializable
data class GuildLevelMember(
    val base: Int = 50,
    val increment: Int = 10
)

@Serializable
data class GuildLevelClaim(
    val base: Int = 20,
    val increment: Int = 10
)

@Serializable
data class GuildLevelUpgradeCost(
    val base: Int = 100,
    val increment: Int = 10
)

@Serializable
data class Chat(
    val muteTag: String = "muted",
    val format: String = "<papi:player_name>: <message>",
    val mentionAllKeyword: String = "all",
    val mentionAllFormat: String = "<gold>@所有人</gold>",
    val titleWhenMentioned: Boolean = true,
    val soundWhenMentioned: Boolean = true,
    val mentionSelfFormat: String = "<gold>@<mentioned></gold>",
    val mentionOtherFormat: String = "<aqua>@<mentioned></aqua>",
    val itemPlaceholderFormat: String = "<dark_gray>[<green><item><dark_gray>]</dark_gray>",
    val inventoryPlaceholderFormat: String = "<dark_gray>[<green><player> 的背包<dark_gray>]</dark_gray>",
    val enderChestPlaceholderFormat: String = "<dark_gray>[<pink><player> 的末影箱<dark_gray>]</dark_gray>",
    val commandPlaceholderFormat: String = "<dark_gray>[<yellow><command><dark_gray>]</dark_gray>",
    val copyPlaceholderFormat: String = "<dark_gray>[<dark_green><text><dark_gray>]</dark_gray>",
    val linkPlaceholderFormat: String = "<dark_gray>[<blue><u><link><dark_gray>]</dark_gray>",
    val placeholders: Map<String, String> = mutableMapOf(
        "blorius" to "<dark_gray>[<blue><papi:playerpoints_points> 络琅<dark_gray>]"
    ),
)

@Serializable
data class Mail(
    val notifyDelaySeconds: Int = 2,
    val unreadTips: Boolean = true,
    val coinsClaimable: Boolean = true,
    val bloriusClaimable: Boolean = true,
    val itemsClaimable: Boolean = true,
)

@Serializable
data class Messaging(
    val host: String = "127.0.0.1",
    val port: Int = 11732,
    val serverName: String = "server",
    val password: String = "X1$&al1*&lakd*#@kak!LKD",
)

@Serializable
data class Security(
    val ensureAuthorized: Boolean = false,
)

@Serializable
data class Modules(
    val mail: Boolean = true
)

@Serializable
data class Database(
    val host: String = "",
    val port: Int = 3306,
    val username: String = "",
    val password: String = "",
    val database: String = "",
    val maxLifeTime: Long = 600000,
    val jdbcUrl: String = "jdbc:postgresql://%host%:%port%/%database%?autoReconnect=true&zeroDateTimeBehavior=convertToNull",
) {

    fun buildDataSource(): HikariDataSource {
        return HikariDataSource(
            HikariConfig().apply {
                this@apply.username = this@Database.username
                this@apply.password = this@Database.password

                this@apply.poolName = "Blora PostgreSQL Connection Pool"

                this@apply.driverClassName = "org.postgresql.Driver"
                this@apply.jdbcUrl = this@Database.jdbcUrl.replace("%host%", host)
                    .replace("%port%", "${this@Database.port}")
                    .replace("%database%", this@Database.database)

                this@apply.addDataSourceProperty("cachePrepStmts", "true")
                this@apply.addDataSourceProperty("prepStmtCacheSize", "250")
                this@apply.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

                this@apply.maxLifetime = this@Database.maxLifeTime
            }
        )
    }

    fun verify(): Boolean {
        try {
            val dataSource = this.buildDataSource()
            dataSource.connection.close()
            dataSource.close()
            return true
        } catch (e: Exception) {
            return false
        }
    }

}