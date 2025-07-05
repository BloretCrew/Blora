package blora.options

import blora.BloraPlugin
import com.velocitypowered.api.proxy.Player
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString

object OptionsFunctions {

    fun updatePlayerOptions(player: Player, data: NbtCompound) {
        val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)!!
        BloraPlugin.database.trans {
            databasePlayer.jsonOptions = databasePlayer.jsonOptions.apply {
                this.alwaysLobby =
                    OptionStatus.valueOf((data["blora_playerOptions_alwaysLobby"] as NbtString).value.uppercase())
            }
            databasePlayer.flush()
        }
    }

}