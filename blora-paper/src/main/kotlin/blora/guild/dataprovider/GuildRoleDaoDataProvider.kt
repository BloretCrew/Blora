package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildRoleDao
import blora.menu.v2.page.PageableDataProvider

class GuildRoleDaoDataProvider(val gid: String): PageableDataProvider<GuildRoleDao> {

    private var loadedData = listOf<GuildRoleDao>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildRoleDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = DB.listRolesForGuild(this.gid)
    }

}