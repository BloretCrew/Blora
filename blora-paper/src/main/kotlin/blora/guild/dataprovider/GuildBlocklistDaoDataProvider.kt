package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildBlocklistDao
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity

class GuildBlocklistDaoDataProvider(
    val guild: String
) : PageableDataProvider<GuildBlocklistDao> {

    private val removedFilter: MutableList<GuildBlocklistDao> = mutableListOf()
    private var loadedData = listOf<GuildBlocklistDao>()

    private var hookedMenu: Menu? = null

    private val hook: (EntityChange) -> Unit = { change ->
        if (change.entityClass == GuildBlocklistDao) {
            val entity = change.toEntity(GuildBlocklistDao)
            if (change.changeType == EntityChangeType.Removed && entity != null) {
                this.removedFilter.add(entity)
                this.hookedMenu?.rerender()
            }
        }
    }

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildBlocklistDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = DB.listBlocklist(this.guild)
            .filter { !removedFilter.contains(it) }
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