package blora.formula

import blora.extension.containsNumberOnly

object FormulaTokenizer {

    fun tokenize(input: String): List<FormulaToken>? {
        val tokens = mutableListOf<FormulaToken>()
        val reader = StringReader(input)
        while (reader.readable()) {
            val char = reader.read()
            if (char.isWhitespace())
                continue
            val lastToken = tokens.lastOrNull()
            if (char == '(') {
                var moreBrackets = 0
                val formula = StringBuilder()
                var formulaOk = false
                while (reader.readable()) {
                    val nextChar = reader.read()
                    if (nextChar == '(')
                        moreBrackets++
                    if (nextChar == ')') {
                        moreBrackets--
                    }
                    if (moreBrackets == -1) {
                        formulaOk = true
                        break
                    }
                    formula.append(nextChar)
                }
                if (!formulaOk)
                    return null
                val tokenizedFormula = tokenize(formula.toString())
                if (tokenizedFormula == null)
                    return null
                if (lastToken != null && lastToken.isValue)
                    return null
                if (tokenizedFormula.size == 1 && tokenizedFormula[0].isValue) {
                    tokens.add(tokenizedFormula[0])
                } else {
                    tokens.add(FormulaToken.ValueToken.Formula(Formula(tokenizedFormula), false))
                }
                continue
            }
            if (char == '+') {
                if (lastToken == null || !lastToken.isValue)
                    return null
                tokens.add(FormulaToken.OperatorToken.Plus)
                continue
            }
            if (char == '*') {
                if (lastToken == null || !lastToken.isValue)
                    return null
                tokens.add(FormulaToken.OperatorToken.Times)
                continue
            }
            if (char == '/') {
                if (lastToken == null || !lastToken.isValue)
                    return null
                tokens.add(FormulaToken.OperatorToken.Division)
                continue
            }
            if (char == '^') {
                if (lastToken == null || !lastToken.isValue)
                    return null
                tokens.add(FormulaToken.OperatorToken.Power)
                continue
            }
            if (char == '%') {
                if (lastToken == null || !lastToken.isValue)
                    return null
                tokens.add(FormulaToken.OperatorToken.Remainder)
                continue
            }
            if (char == '-') {
                if (lastToken == null) {
                    if (reader.peek() == '(') {
                        reader.skip()
                        var moreBrackets = 0
                        val formula = StringBuilder()
                        var formulaOk = false
                        while (reader.readable()) {
                            val nextChar = reader.read()
                            if (nextChar == '(')
                                moreBrackets++
                            if (nextChar == ')') {
                                moreBrackets--
                            }
                            if (moreBrackets == -1) {
                                formulaOk = true
                                break
                            }
                            formula.append(nextChar)
                        }
                        if (!formulaOk)
                            return null
                        val tokenizedFormula = tokenize(formula.toString())
                        if (tokenizedFormula == null)
                            return null
                        if (tokenizedFormula.size == 1 && tokenizedFormula[0].isValue) {
                            tokens.add(tokenizedFormula[0])
                        } else {
                            tokens.add(FormulaToken.ValueToken.Formula(Formula(tokenizedFormula), true))
                        }
                    } else {
                        val text = reader.readUntilNotLetterOrDigitOrUnderline()
                        if (text.containsNumberOnly()) {
                            tokens.add(FormulaToken.ValueToken.Constant(-text.toDouble()))
                        } else {
                            tokens.add(FormulaToken.ValueToken.Variable(text, true))
                        }
                    }
                } else {
                    if (!lastToken.isValue)
                        return null
                    tokens.add(FormulaToken.OperatorToken.Minus)
                }
            } else {
                if (lastToken != null && lastToken.isValue)
                    return null
                val text = char + reader.readUntilNotLetterOrDigitOrUnderline()
                if (text.containsNumberOnly()) {
                    tokens.add(FormulaToken.ValueToken.Constant(text.toDouble()))
                } else {
                    tokens.add(FormulaToken.ValueToken.Variable(text, false))
                }
            }
        }

        return tokens.toList()
    }

    fun parse(input: String): Formula? {
        val tokens = tokenize(input)
        return if (tokens != null) Formula(tokens) else null
    }

}