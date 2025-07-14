package blora.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationContents(
    val mail: Mail = Mail(),
    val messageing: Messaging = Messaging(),
    val security: Security = Security(),
    val modules: Modules = Modules(),
    val database: Database = Database(),
)

@Serializable
data class Mail(
    val unreadTips: Boolean = true
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
    val jdbcUrl: String = "jdbc:mariadb://%host%:%port%/%database%?autoReconnect=true&zeroDateTimeBehavior=convertToNull",
) {

    fun buildDataSource(): HikariDataSource {
        return HikariDataSource(
            HikariConfig().apply {
                this@apply.username = this@Database.username
                this@apply.password = this@Database.password

                this@apply.poolName = "Blora MariaDB Connection Pool"

                this@apply.driverClassName = "org.mariadb.jdbc.Driver"
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