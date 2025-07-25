package blora.guild.formula

import blora.database.guild.dao.GuildDao
import blora.formula.FormulaVariable

class GuildFormulaVariable(val guild: GuildDao) : FormulaVariable {

    override val id: String
        get() = "guild"

    override fun parse(arguments: Array<String>): Double? {
        if (arguments.size != 1)
            return null
        return when (arguments[0]) {
            "balance" -> this.guild.bankBalance
            "max_balance" -> this.guild.bankBalanceMax
            "level" -> this.guild.level.toDouble()
            "vitality" -> this.guild.vitality
            "members" -> this.guild.members.size.toDouble()
            "blocklist" -> this.guild.blocklist.size.toDouble()
            "towns" -> this.guild.towns.size.toDouble()
            "allys" -> this.guild.allys.size.toDouble()
            else -> null
        }
    }

}