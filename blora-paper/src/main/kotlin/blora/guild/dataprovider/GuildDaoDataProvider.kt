package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.menu.v2.page.PageableDataProvider

class GuildDaoDataProvider(
    val filter: (guild: GuildDao) -> Boolean,
) : PageableDataProvider<GuildDao> {

    private var loadedData = listOf<GuildDao>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = DB.listGuilds().filter { filter(it) }
    }

}