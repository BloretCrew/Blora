package blora.redeem.dataprovider

import blora.database.redeem.dao.RedeemDao
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity

class RedeemDaoDataProvider : PageableDataProvider<RedeemDao> {

    private val removedFilter: MutableList<RedeemDao> = mutableListOf()
    private var loadedData = listOf<RedeemDao>()

    private var hookedMenu: Menu? = null

    private val hook: (EntityChange) -> Unit = { change ->
        if (change.entityClass == RedeemDao) {
            val entity = change.toEntity(RedeemDao)
            if (change.changeType == EntityChangeType.Removed && entity != null) {
                this.removedFilter.add(entity)
            }
            this.hookedMenu?.rerender()
        }
    }

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): RedeemDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = RedeemDao.list()
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