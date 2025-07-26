package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildAllyRequestDao
import blora.database.guild.dao.GuildDao
import blora.database.guild.table.GuildAllyRequestTable
import blora.database.guild.table.GuildTable
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity

class GuildAllyRequestDaoProvider(
    val guild: GuildDao
) : PageableDataProvider<Pair<GuildAllyRequestDao, GuildDao>> {

    private val finishedFilter: MutableList<GuildAllyRequestDao> = mutableListOf()
    private var loadedData = listOf<Pair<GuildAllyRequestDao, GuildDao>>()

    private var hookedMenu: Menu? = null

    private val hook: (EntityChange) -> Unit = { change ->
        if (change.entityClass == GuildAllyRequestDao) {
            val entity = change.toEntity(GuildAllyRequestDao)
            if (entity != null && entity.finished) {
                this.finishedFilter.add(entity)
                this.hookedMenu?.rerender()
            }
        }
    }

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): Pair<GuildAllyRequestDao, GuildDao> {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData =
            DB.listValidAllyRequestsForGuild(this.guild.gid)
                .filter { !finishedFilter.contains(it) }
                .map { it to DB.getGuildByGid(it.requestGuildId)!! }
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