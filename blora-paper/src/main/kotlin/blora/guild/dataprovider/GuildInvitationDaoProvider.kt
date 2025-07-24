package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildInvitationDao
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity
import java.util.*

class GuildInvitationDaoProvider(
    val player: UUID
) : PageableDataProvider<GuildInvitationDao> {

    private val removedFilter: MutableList<GuildInvitationDao> = mutableListOf()
    private var loadedData = listOf<GuildInvitationDao>()

    private var hookedMenu: Menu? = null

    private val hook: (EntityChange) -> Unit = { change ->
        if (change.entityClass == GuildInvitationDao) {
            val entity = change.toEntity(GuildInvitationDao)
            if (change.changeType == EntityChangeType.Removed && entity != null) {
                this.removedFilter.add(entity)
            }
            this.hookedMenu?.rerender()
        }
    }

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildInvitationDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        val preInvites = DB.listGuildInvites(this.player)
        val guilds = preInvites.map { it.guildId }
            .mapNotNull { DB.getGuildByGid(it) }
            .associateBy { it.gid }
        this.loadedData = preInvites.filter {
            val guild = guilds[it.guildId]
            if (guild != null) {
                if (guild.blocklist.contains(this.player)) {
                    DB.trans {
                        it.delete()
                    }
                    return@filter false
                }
            }
            return@filter true
        }.filter { !removedFilter.contains(it) }
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