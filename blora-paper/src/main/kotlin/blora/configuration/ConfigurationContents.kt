package blora.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationContents(
    val chat: Chat = Chat(),
    val mail: Mail = Mail(),
    val messaging: Messaging = Messaging(),
    val security: Security = Security(),
    val modules: Modules = Modules(),
    val database: Database = Database(),
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
    /**
     * Commands that cannot be used in chat `<cmd:...>` suggestions.
     * Match is case-insensitive against the first token (with or without leading `/`,
     * with or without namespace, e.g. `kill`, `/kill`, `minecraft:kill`).
     */
    val commandPlaceholderDenyList: List<String> = listOf(
        "kill",
        "killall",
        "stop",
        "restart",
        "op",
        "deop",
        "ban",
        "ban-ip",
        "pardon",
        "pardon-ip",
        "whitelist",
        "gamemode",
        "gm",
        "give",
        "xp",
        "experience",
        "clear",
        "execute",
        "function",
        "datapack",
        "reload",
        "luckperms",
        "lp",
        "perms",
        "permission",
    ),
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
