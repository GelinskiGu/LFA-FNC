fun main() {
    var grammar: Grammar ? = defineGrammar()

    while (true) {
        println("Escolha uma opção:")
        println("1 - Aplicar a 2° etapa da regra de Chomsky")
        println("2 - Aplicar a 3° etapa da regra de Chomsky")
        println("3 - Alterar gramática")
        println("4 - Imprimir gramática")
        println("5 - Sair")
        val input = readlnOrNull()?.trim() ?: break
        when (input) {
            "1" -> {
                grammar?.chomskyTwoOrGreater()
            }
            "2" -> {
                grammar?.createChomskyRulesForThreeOrGreater()
                grammar?.rules?.filter { it.left.startsWith("Y") }?.forEach { rule -> grammar?.replaceSymbol(rule) }
            }
            "3" -> {
                grammar = defineGrammar()
            }
            "4" -> {
                grammar?.printGrammar()
            }
            "5" -> {
                break
            }
            else -> {
                println("Opção inválida")
                break
            }
        }
    }
}

fun defineGrammar(): Grammar? {
    var grammar: Grammar ? = null
    println("Escolha uma opção para definir a gramática:")
    println("1 - Utilizar a gramática padrão")
    println("2 - Definir a gramática manualmente")
    val input = readlnOrNull() ?: return null
    when (input) {
        "1" -> {
            return mockGrammar()
        }
        "2" -> {
            grammar = Grammar(mutableSetOf(), setOf(), mutableListOf(), "")
            println("Digite as variáveis não terminais da gramática:")
            val nonTerminals = readlnOrNull()?.split(" ", ignoreCase = true)?.toMutableSet() ?: return null
            grammar.nonTerminals.addAll(nonTerminals)
            println("Digite os terminais da gramática:")
            val terminals = readlnOrNull()?.split(" ", ignoreCase = true)?.toSet() ?: return null
            grammar.terminals = terminals
            println("Digite o símbolo inicial da gramática:")
            val startSymbol = readlnOrNull() ?: return null
            grammar.startSymbol = startSymbol
            println("Digite o número de regras da gramática:")
            val numberOfRules = readlnOrNull()?.toIntOrNull() ?: return null
            println("Digite as regras da gramática:")
            for (i in 0..<numberOfRules) {
                val rule = readlnOrNull() ?: break
                val (left, right) = rule.split("->")
                println("Regra: $left -> $right")
                val rightList = right.split(" ").toMutableList()
                val newRule = Rule(left, rightList)
                grammar.addRule(newRule)
                println("Regra ${newRule.printRule()} adicionada com sucesso!")
            }
        }
        else -> println("Opção inválida")
    }
    return grammar
}

data class Rule(val left: String, val right: MutableList<String>) {
    fun isSizeTwoOrGreaterWithTerminal(terminals: Set<String>): Boolean {
        right.find { it in terminals } ?: return false
        return right.size >= 2
    }

    fun isSizeThreeOrGreater(): Boolean {
        return right.size >= 3
    }

    fun printRule(): String {
        return "$left -> ${right.joinToString(" ")}"
    }
}

class Grammar(
    val nonTerminals: MutableSet<String>,
    var terminals: Set<String>,
    val rules: MutableList<Rule>,
    var startSymbol: String
) {
    fun addRule(rule: Rule) {
        rules.add(rule)
    }

    private fun addNonTerminal(nonTerminal: String) {
        nonTerminals.add(nonTerminal)
    }

    fun removeRule(rule: Rule) {
        rules.remove(rule)
    }

    fun chomskyTwoOrGreater() {
        createChomskyRulesUsingTerminals()
        for (rule in rules) {
            if (rule.isSizeTwoOrGreaterWithTerminal(terminals)) {
                rule.right.replaceAll { if (it in terminals) "X$it" else it }
            }
        }
    }

    private fun createChomskyRulesUsingTerminals() {
        terminals.forEach {
            val rule = Rule("X$it", mutableListOf(it))
            if (!rules.contains(rule)) {
                addNonTerminal("X$it")
                addRule(rule)
            }
        }
    }

    fun createChomskyRulesForThreeOrGreater() {
        val newRules = mutableListOf<Rule>()
        rules.forEach {
            if (it.isSizeThreeOrGreater()) {
                val newNonTerminal = "Y_(${it.right[it.right.size - 2]}${it.right.last()})"
                val newRule = Rule(newNonTerminal, it.right.subList(it.right.size - 2, it.right.size))
                if (newNonTerminal !in nonTerminals) addNonTerminal(newNonTerminal)
                if (newRule !in newRules) newRules.add(newRule)
            }
        }
        rules.addAll(newRules)
    }

    fun replaceSymbol(rule: Rule) {
        val comparator = getComparator(rule.left)
        val rulesToBeAdded = mutableListOf<Rule>()
        val rulesToBeRemoved = mutableListOf<Rule>()
        rules.forEach {
            if (!it.left.startsWith("Y") && it.right.containsAll(rule.right) && it.right.size > 2) {
                val mutableList = it.right.toMutableList()
                for (i in 1..<mutableList.size - 1) {
                    if ((mutableList[i].first() == comparator.first()) && (mutableList[i + 1].first() == comparator.last())) {
                        mutableList[i] = rule.left
                        mutableList.removeAt(i + 1)
                    }
                    rulesToBeRemoved.add(it)
                }
                rulesToBeAdded.add(Rule(it.left, mutableList))
            }
        }
        rules.addAll(rulesToBeAdded)
        rules.removeAll(rulesToBeRemoved)
    }

    private fun getComparator(nonTerminal: String): String {
        val startIndex = nonTerminal.indexOf("(") + 1
        val endIndex = nonTerminal.indexOf(")")
        return nonTerminal.substring(startIndex, endIndex)
    }

    fun printGrammar() {
        println("V: {${nonTerminals.joinToString(", ")}}")
        println("T: {${terminals.joinToString(", ")}}")
        println("S: $startSymbol")
        println("P: {")
        this.rules.sortedBy { it.left }.forEach { println(it.printRule()) }
        println("}")
    }
}

fun mockGrammar(): Grammar {
    val nonTerminals = mutableSetOf("S", "A", "B")
    val terminals = setOf("a", "b")
    val rules = mutableListOf(
        Rule("S", mutableListOf("A", "S", "B")),
        Rule("S", mutableListOf("A", "B")),
        Rule("A", mutableListOf("a", "A", "S")),
        Rule("A", mutableListOf("a")),
        Rule("A", mutableListOf("a", "A")),
        Rule("B", mutableListOf("S", "b", "S")),
        Rule("B", mutableListOf("b", "b")),
        Rule("B", mutableListOf("S", "b")),
        Rule("B", mutableListOf("b", "S")),
        Rule("B", mutableListOf("b")),
        Rule("B", mutableListOf("a", "A", "S")),
        Rule("B", mutableListOf("a")),
        Rule("B", mutableListOf("a", "A"))
    )
    return Grammar(nonTerminals, terminals, rules, "S")
}