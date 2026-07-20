package blora.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationContents(
    val administration: Administration = Administration(),
    val dialogs: Dialogs = Dialogs(),
    val chat: Chat = Chat(),
    val mail: Mail = Mail(),
    val server: Server = Server(),
    val messaging: Messaging = Messaging(),
    val authorization: Authorization = Authorization(),
    val security: Security = Security(),
    val database: Database = Database()
)

@Serializable
data class Dialogs(
    val eulaWidth: Int = 200
)

@Serializable
data class Chat(
    val privateMessageReceiveFormat: String = "<click:suggest_command:'/tell <sender> '><hover:show_text:'<yellow>点击复制'><sender> -> 您</hover></click>：<message>",
    val privateMessageSendFormat: String = "您 -> <receiver>：<message>",
    val privateMessageSpyFormat: String = "<sender> 向玩家 <receiver> 私聊：<message>",
    val privateMessageSpyEnabledMessage: String = "<green>已开启私聊监听",
    val privateMessageSpyDisabledMessage: String = "<red>已关闭私聊监听",
)

@Serializable
data class Mail(
    val senderNameFormat: String = "<username>"
)

@Serializable
data class Administration(
    val debug: Boolean = false,
)

@Serializable
data class Server(
    val limbo: String = "limbo",
    val lobby: String = "lobby",
)

@Serializable
data class Messaging(
    val port: Int = 11732,
    val password: String = "X1$&al1*&lakd*#@kak!LKD",
)

@Serializable
data class Authorization(
    val onlineFeatures: Boolean = false,
    val alwaysLobby: Boolean = false,
    val checkUsername: Boolean = true,
    val usernameRegex: String = "[0-9a-zA-Z_]*",
    val minUsernameLength: Int = 3,
    val maxUsernameLength: Int = 16,
    val uuidGenerator: UUIDGenerator = UUIDGenerator.MOJANG
)

@Serializable
data class Security(
    val allowOnlinePlayerAutoLogin: Boolean = true,
    val sameIpAutoLogin: Boolean = false,
    val autoLoginExpireTime: Long = 600L,
    val minPasswordLength: Int = 8,
    val maxPasswordLength: Int = 32,
    val weakPasswords: List<String> = listOf(
        "12345678",
        "abcdefgh",
        "password"
    ),
    val passwordStrategy: List<String> = listOf(
        "noUsername"
    ),
    val ipLimit: Int = -1,
    val ipLimitDisableRegister: Boolean = false,
    val ipLimitDisableLogin: Boolean = false,
    val ipLimitStrategyForRegister: Order = Order.FIRST,
    val maxRetries: Int = 3,
    val maxNotLogin: Int = 300,
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

enum class UUIDGenerator {

    MOJANG, CRACKED

}

enum class Order {
    FIRST, LAST
}