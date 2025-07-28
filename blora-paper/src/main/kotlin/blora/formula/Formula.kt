package blora.formula

class Formula(
    private val tokens: List<FormulaToken>,
) {

    fun calculate(vararg variables: FormulaVariable): Double? {
        val copiedTokens = this.tokens.toMutableList()
        for (priority in 4 downTo 1) {
            while (copiedTokens.any { it is FormulaToken.OperatorToken && it.priority == priority }) {
                val tokenIndex =
                    copiedTokens.indexOfFirst { it is FormulaToken.OperatorToken && it.priority == priority }
                val operatorToken = copiedTokens[tokenIndex] as FormulaToken.OperatorToken
                val previous = copiedTokens[tokenIndex - 1] as FormulaToken.ValueToken
                val next = copiedTokens[tokenIndex + 1] as FormulaToken.ValueToken

                val previousValue = previous.value(*variables)
                val nextValue = next.value(*variables)

                if (previousValue == null || nextValue == null)
                    return null

                val value = FormulaToken.ValueToken.Constant(operatorToken.operate(previousValue, nextValue))
                copiedTokens[tokenIndex - 1] = value
                copiedTokens.removeAt(tokenIndex) // remove operator
                copiedTokens.removeAt(tokenIndex) // operator removed, next moved left, remove next value
            }
        }
        if (copiedTokens.size != 1)
            return null
        val value = copiedTokens[0]
        if (value !is FormulaToken.ValueToken)
            return null
        return value.value(*variables)
    }

}
/*
fun main() {
    val formula1 = FormulaTokenizer.parse("100 * 8")
    val formula2 = FormulaTokenizer.parse("test + 100 * 8")
    val formula3 = FormulaTokenizer.parse("(test + 100) * 8")
    val formula4 = FormulaTokenizer.parse("(test + 100) * 8 + (8 * (2 + 3))")
    println(formula1?.calculate())
    println(formula2?.calculate(FormulaVariable.simple("test", 123.0)))
    println(formula3?.calculate(FormulaVariable.simple("test", 123.0)))
    println(formula4?.calculate(FormulaVariable.simple("test", 123.0)))
}
*/
