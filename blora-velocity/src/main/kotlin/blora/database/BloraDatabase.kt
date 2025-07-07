package blora.database

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class BloraDatabase(
    private val dataSource: HikariDataSource,
) {

    val database = Database.connect(dataSource)

    fun <T> trans(statement: Transaction.() -> T): T {
        return transaction(database, statement)
    }

    fun initTables() {
        trans {
            SchemaUtils.create(PlayerTable)
        }
    }

    fun getPlayerByName(name: String): BloraPlayer? {
        return trans {
            val result = BloraPlayer.find {
                PlayerTable.username eq name.lowercase()
            }
            if (result.count() == 0L) {
                return@trans null
            }
            return@trans result.toList()[0]
        }
    }

    fun getPlayerByUuid(uuid: UUID): BloraPlayer? {
        return trans {
            val result = BloraPlayer.find {
                PlayerTable.uuid eq uuid
            }
            if (result.count() == 0L) {
                return@trans null
            }
            return@trans result.toList()[0]
        }
    }

    fun getPlayerByPremiumUuid(uuid: UUID): BloraPlayer? {
        return trans {
            val result = BloraPlayer.find {
                PlayerTable.premiumUuid eq uuid
            }
            if (result.count() == 0L) {
                return@trans null
            }
            return@trans result.toList()[0]
        }
    }

    fun getFirstIpAmount(ip: String): Int {
        return trans {
            BloraPlayer.find {
                PlayerTable.firstIp eq ip
            }.count()
                .toInt()
        }
    }

    fun getLastIpAmount(ip: String): Int {
        return trans {
            BloraPlayer.find {
                PlayerTable.lastIp eq ip
            }.count()
                .toInt()
        }
    }

    fun disconnect() {
        this.dataSource.close()
    }

}