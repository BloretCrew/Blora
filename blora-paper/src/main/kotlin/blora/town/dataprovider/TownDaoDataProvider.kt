package blora.town.dataprovider

import blora.database.town.dao.TownDao
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity

class TownDaoDataProvider : PageableDataProvider<TownDao> {

    private val removedFilter: MutableList<TownDao> = mutableListOf()
    private var loadedData = listOf<TownDao>()

    private var hookedMenu: Menu? = null

    private val hook: (EntityChange) -> Unit = { change ->
        if (change.entityClass == TownDao) {
            val entity = change.toEntity(TownDao)
            if (change.changeType == EntityChangeType.Removed && entity != null) {
                this.removedFilter.add(entity)
            }
            this.hookedMenu?.rerender()
        }
    }

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): TownDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = TownDao.listAll()
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