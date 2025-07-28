package blora.town.dataprovider

import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildRoleDao
import blora.database.town.dao.TownDao
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import blora.town.TownTarget
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity

class TownSpecificRoleDataProvider(
    val guild: GuildDao,
    val town: TownDao,
) : PageableDataProvider<GuildRoleDao> {

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
        this.loadedData = GuildRoleDao.listByGuildId(guild.gid)
            .filter {
                !town.permissionContainers.any { tpc ->
                    tpc.target is TownTarget.SpecificRole && tpc.target.roleId == it.roleId
                }
            }
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