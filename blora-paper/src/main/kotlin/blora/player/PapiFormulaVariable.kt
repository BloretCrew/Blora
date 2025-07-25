package blora.player

import blora.formula.FormulaVariable
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.OfflinePlayer

class PapiFormulaVariable(val player: OfflinePlayer) : FormulaVariable {

    override val id: String
        get() = "papi"

    override fun parse(arguments: Array<String>): Double? {
        if (arguments.isEmpty())
            return null
        return PlaceholderAPI.setPlaceholders(player, "%${arguments.joinToString(":")}%").toDoubleOrNull()
    }

}