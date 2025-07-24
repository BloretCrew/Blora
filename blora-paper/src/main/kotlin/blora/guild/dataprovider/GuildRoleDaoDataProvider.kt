package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildRoleDao
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity

class GuildRoleDaoDataProvider(val gid: String) : PageableDataProvider<GuildRoleDao> {

    private val removedFilter: MutableList<GuildRoleDao> = mutableListOf()
    private var loadedData = listOf<GuildRoleDao>()

    private var hookedMenu: Menu? = null

    private val hook: (EntityChange) -> Unit = { change ->
        if (change.entityClass == GuildRoleDao) {
            val entity = change.toEntity(GuildRoleDao)
            if (change.changeType == EntityChangeType.Removed && entity != null) {
                this.removedFilter.add(entity)
            }
            this.hookedMenu?.rerender()
        }
    }

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildRoleDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = DB.listRolesForGuild(this.gid)
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