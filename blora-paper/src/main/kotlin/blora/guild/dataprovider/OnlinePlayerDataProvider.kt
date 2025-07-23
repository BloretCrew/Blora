package blora.guild.dataprovider

import blora.menu.v2.page.PageableDataProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class OnlinePlayerDataProvider(
    val filter: (Player) -> Boolean = { _ -> true },
) : PageableDataProvider<Player> {

    private var loadedData = listOf<Player>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): Player {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = Bukkit.getOnlinePlayers().filter { filter(it) }
    }

}