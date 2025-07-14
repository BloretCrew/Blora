package blora.plugin

import net.milkbowl.vault.economy.Economy
import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.PlayerPointsAPI
import org.bukkit.Bukkit

object ThirdPartys {

    val vaultApi: Economy
        get() = economyProvider
    val playerPoints: PlayerPointsAPI
        get() = PlayerPoints.getInstance().api

    private lateinit var economyProvider: Economy

    fun init() {
        this.economyProvider = Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)!!.provider
    }

}