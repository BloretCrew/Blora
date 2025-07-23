package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildJoinRequestDao
import blora.menu.v2.page.PageableDataProvider

class ValidGuildJoinRequestDaoDataProvider(
    val guild: String
) : PageableDataProvider<GuildJoinRequestDao> {

    private var loadedData = listOf<GuildJoinRequestDao>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildJoinRequestDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = DB.listValidJoinRequestsForGuild(this.guild)
    }

}