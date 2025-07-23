package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildBlocklistDao
import blora.menu.v2.page.PageableDataProvider

class GuildBlocklistDaoDataProvider(
    val guild: String
) : PageableDataProvider<GuildBlocklistDao> {

    private var loadedData = listOf<GuildBlocklistDao>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildBlocklistDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = DB.listBlocklist(this.guild)
    }

}