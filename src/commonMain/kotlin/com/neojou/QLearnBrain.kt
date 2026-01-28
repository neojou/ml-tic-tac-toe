package com.neojou

private fun boardToString(b: BoardStatus): String =
    b.copyArray().joinToString(prefix = "[", postfix = "]", separator = ",")

class QLearnBrain {
    // key = board string, value = Q-values for 9 actions
    private val qTable: MutableMap<String, DoubleArray> = mutableMapOf()

    fun keyOf(board: BoardStatus): String = boardToString(board)

    fun qValuesOf(key: String): DoubleArray =
        qTable.getOrPut(key) { DoubleArray(9) { 0.0 } }

    fun clear() = qTable.clear()

    fun size(): Int = qTable.size
}
