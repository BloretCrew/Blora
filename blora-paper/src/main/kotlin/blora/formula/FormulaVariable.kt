package blora.formula

interface FormulaVariable {

    val id: String

    fun parse(arguments: Array<String>): Double?

    companion object {

        fun simple(name: String, value: Double): FormulaVariable {
            return object : FormulaVariable {
                override val id: String = name
                override fun parse(arguments: Array<String>): Double? {
                    return value
                }
            }
        }

    }

}