package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildMemberInfoDao
import blora.menu.v2.page.PageableDataProvider

class GuildMemberDaoWithRolePriorityDataProvider(
    val guild: GuildDao
) : PageableDataProvider<Pair<GuildMemberInfoDao, Int>> {

    private var loadedData = listOf<Pair<GuildMemberInfoDao, Int>>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): Pair<GuildMemberInfoDao, Int> {
        return this.loadedData[index]
    }

    override fun refresh() {
        val memberMaxRolePriority = DB.listRolePriorities(this.guild)
        this.loadedData = DB.listMemberInfo(this.guild.gid).map { it to memberMaxRolePriority[it.player]!! }
    }

}