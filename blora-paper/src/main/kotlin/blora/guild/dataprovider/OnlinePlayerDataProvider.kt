package blora.guild.dataprovider

import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import blora.plugin.BloraPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class OnlinePlayerDataProvider(
    val filter: (Player) -> Boolean = { _ -> true },
) : PageableDataProvider<Player>, Listener {

    private var loadedData = listOf<Player>()

    private var hookedMenu: Menu? = null

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): Player {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = Bukkit.getOnlinePlayers().filter { filter(it) }
    }

    override fun hook(menu: Menu) {
        this.hookedMenu = menu
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
    }

    override fun unhook() {
        this.hookedMenu = null
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        this.hookedMenu?.rerender()
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        this.hookedMenu?.rerender()
    }

}