package blora.guild.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildInviteCodeDao
import blora.menu.v2.page.PageableDataProvider
import java.time.LocalDate

class GuildInviteCodeDaoDataProvider(
    val guild: String
) : PageableDataProvider<GuildInviteCodeDao> {

    private var loadedData = listOf<GuildInviteCodeDao>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildInviteCodeDao {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = DB.listInvitationCodes(this.guild)
            .filter {
                if (it.expireAt != null && it.expireAt!! < LocalDate.now() && !it.outdated) {
                    DB.trans {
                        it.outdated = true
                        it.flush()
                    }
                    return@filter true
                }
                return@filter true
            }
            .filter { !it.outdated }
    }

}