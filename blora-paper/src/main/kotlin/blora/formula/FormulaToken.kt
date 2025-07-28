package blora.formula

import java.util.regex.Pattern
import kotlin.math.pow

sealed class FormulaToken {

    abstract val isValue: Boolean

    sealed class ValueToken : FormulaToken() {

        override val isValue: Boolean = true

        abstract fun value(vararg variables: FormulaVariable): Double?

        class Formula(val formula: blora.formula.Formula, val opposite: Boolean) : ValueToken() {
            override fun value(vararg variables: FormulaVariable): Double? {
                val value = this.formula.calculate(*variables)
                return if (value != null) if (opposite) -value else value else null
            }
        }

        class Variable(val name: String, val opposite: Boolean) : ValueToken() {
            override fun value(vararg variables: FormulaVariable): Double? {
                val (id: String, args: Array<String>) = if (name.contains(":")) {
                    val split = name.split(Pattern.compile(":"), 2)
                    split[0] to split[1].split(":").toTypedArray()
                } else {
                    name to emptyArray<String>()
                }
                for (variableProvider in variables) {
                    if (variableProvider.id == id) {
                        val value = variableProvider.parse(args)
                        if (value == null)
                            return null
                        return if (this.opposite) {
                            -value
                        } else {
                            value
                        }
                    }
                }
                return null
            }
        }

        class Constant(val value: Double) : ValueToken() {
            override fun value(vararg variables: FormulaVariable): Double? {
                return this.value
            }
        }

    }

    sealed class OperatorToken : FormulaToken() {

        override val isValue: Boolean = false
        abstract val priority: Int

        abstract fun operate(left: Double, right: Double): Double

        object Plus : OperatorToken() {
            override val priority: Int = 1
            override fun operate(left: Double, right: Double): Double {
                return left + right
            }
        }

        object Minus : OperatorToken() {
            override val priority: Int = 1
            override fun operate(left: Double, right: Double): Double {
                return left - right
            }
        }

        object Remainder : OperatorToken() {
            override val priority: Int = 2
            override fun operate(left: Double, right: Double): Double {
                return left % right
            }
        }

        object Times : OperatorToken() {
            override val priority: Int = 2
            override fun operate(left: Double, right: Double): Double {
                return left * right
            }
        }

        object Division : OperatorToken() {
            override val priority: Int = 2
            override fun operate(left: Double, right: Double): Double {
                return left / right
            }
        }

        object Power : OperatorToken() {
            override val priority: Int = 3
            override fun operate(left: Double, right: Double): Double {
                return left.pow(right)
            }
        }

    }

    companion object {

        val OPERATORS: List<OperatorToken> = listOf(
            OperatorToken.Plus,
            OperatorToken.Minus,
            OperatorToken.Times,
            OperatorToken.Division,
            OperatorToken.Remainder,
            OperatorToken.Power
        )

    }

}