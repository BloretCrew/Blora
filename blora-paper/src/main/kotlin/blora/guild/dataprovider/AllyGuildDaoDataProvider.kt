package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildAllyInfoDao
import blora.database.guild.dao.GuildDao
import blora.database.guild.table.GuildAllyInfoTable
import blora.database.guild.table.GuildTable
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

class AllyGuildDaoDataProvider(val guild: GuildDao) : PageableDataProvider<Pair<GuildDao, GuildAllyInfoDao>> {

    private val removedFilter: MutableList<GuildDao> = mutableListOf()
    private var loadedData = listOf<Pair<GuildDao, GuildAllyInfoDao>>()

    private var hookedMenu: Menu? = null

    private val hook: (EntityChange) -> Unit = { change ->
        if (change.entityClass == GuildDao) {
            val entity = change.toEntity(GuildDao)
            if (change.changeType == EntityChangeType.Removed && entity != null) {
                this.removedFilter.add(entity)
            }
            if (entity != null) {
                if (entity == this.guild || this.guild.allys.contains(entity.gid))
                    this.hookedMenu?.rerender()
            }
        }
        if (change.entityClass == GuildAllyInfoDao::class) {
            val entity = change.toEntity(GuildAllyInfoDao)
            if (entity != null && (entity.requestGuildId == this.guild.gid || entity.receiveGuildId == this.guild.gid))
                this.hookedMenu?.rerender()
        }
    }

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): Pair<GuildDao, GuildAllyInfoDao> {
        return this.loadedData[index]
    }

    override fun refresh() {
        DB.trans {
            this@AllyGuildDaoDataProvider.guild.refresh()
            this@AllyGuildDaoDataProvider.loadedData = guild.allys
                .mapNotNull { gid -> GuildDao.find { GuildTable.gid eq gid }.firstOrNull() }
                .filter { !removedFilter.contains(it) }
                .map { it to GuildAllyInfoDao.find {
                    (
                            (GuildAllyInfoTable.requestGid eq this@AllyGuildDaoDataProvider.guild.gid) and
                            (GuildAllyInfoTable.receiveGid eq it.gid)
                    ) or (
                            (GuildAllyInfoTable.requestGid eq it.gid) and
                                    (GuildAllyInfoTable.receiveGid eq this@AllyGuildDaoDataProvider.guild.gid)
                    )
                }.first() }
        }
    }

    override fun hook(menu: Menu) {
        this.hookedMenu = menu
        EntityHook.subscribe(this.hook)
    }

    override fun unhook() {
        this.hookedMenu = null
        EntityHook.unsubscribe(this.hook)
    }

}