@file:OptIn(ExperimentalUuidApi::class)

package blora.town.dataprovider

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.player.dao.PlayerInfoDao
import blora.database.town.dao.TownDao
import blora.menu.v2.page.PageableDataProvider
import blora.town.TownTarget
import org.bukkit.entity.Player
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

class TownSpecificPlayerDataProvider(
    val player: Player,
    val guild: GuildDao,
    val town: TownDao,
) : PageableDataProvider<Pair<UUID, String>> {

    private var filter: MutableList<UUID> = mutableListOf()
    private var loadedData = listOf<Pair<UUID, String>>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): Pair<UUID, String> {
        return this.loadedData[index]
    }

    override fun refresh() {
        DB.trans {
            guild.refresh()
            town.refresh()
        }
        val currentPlayerMaxPriority = DB.getMaxRolePriority(player.uniqueId, guild)
        this.loadedData = PlayerInfoDao.listAll()
            .filter {
                !town.permissionContainers.any { tpc ->
                    tpc.target is TownTarget.SpecificPlayer && tpc.target.uuid.toJavaUuid() == it.playerUuid
                }
            }
            .filter { !this.filter.contains(it.playerUuid) }
            .filter { it.playerUuid != player.uniqueId }
            .filter { it.playerUuid != guild.owner }
            .filter { currentPlayerMaxPriority > DB.getMaxRolePriority(it.playerUuid, guild) }
            .map { it.playerUuid to it.username }
    }

    internal fun addFiltered(uuid: UUID) {
        this.filter.add(uuid)
    }

}