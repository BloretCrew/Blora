package blora.database

import blora.database.player.PlayerDao
import blora.database.player.PlayerLoginDao
import blora.database.player.PlayerLoginTable
import blora.database.player.PlayerTable
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
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
            SchemaUtils.create(PlayerLoginTable)
        }
    }

    fun playerLoginDateSave(uuid: UUID) {
        trans {
            val result = PlayerLoginDao.find {
                PlayerLoginTable.uuid eq uuid
                PlayerLoginTable.date eq LocalDate.now()
            }
            if (result.count() > 0) {
                return@trans
            }
            PlayerLoginDao.new {
                this.playerUuid = uuid
                this.loginDate = LocalDate.now()
            }
        }
    }

    fun getPlayerByName(name: String): PlayerDao? {
        return trans {
            val result = PlayerDao.find {
                PlayerTable.username eq name.lowercase()
            }
            if (result.count() == 0L) {
                return@trans null
            }
            return@trans result.toList()[0]
        }
    }

    fun getPlayerByUuid(uuid: UUID): PlayerDao? {
        return trans {
            val result = PlayerDao.find {
                PlayerTable.uuid eq uuid
            }
            if (result.count() == 0L) {
                return@trans null
            }
            return@trans result.toList()[0]
        }
    }

    fun getPlayerByPremiumUuid(uuid: UUID): PlayerDao? {
        return trans {
            val result = PlayerDao.find {
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
            PlayerDao.find {
                PlayerTable.firstIp eq ip
            }.count()
                .toInt()
        }
    }

    fun getLastIpAmount(ip: String): Int {
        return trans {
            PlayerDao.find {
                PlayerTable.lastIp eq ip
            }.count()
                .toInt()
        }
    }

    fun disconnect() {
        this.dataSource.close()
    }

}