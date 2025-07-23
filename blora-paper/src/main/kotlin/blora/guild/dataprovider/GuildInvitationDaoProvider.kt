package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildInvitationDao
import blora.menu.v2.page.PageableDataProvider
import java.util.*

class GuildInvitationDaoProvider(
    val player: UUID
) : PageableDataProvider<GuildInvitationDao> {

    private var loadedData = listOf<GuildInvitationDao>()

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
        }
    }

}