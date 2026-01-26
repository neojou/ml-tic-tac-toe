package com.neojou

object TicTacToeRules {

    // 8 條可能連線：3 橫 + 3 直 + 2 斜
    private val LINES: Array<IntArray> = arrayOf(
        intArrayOf(0, 1, 2),
        intArrayOf(3, 4, 5),
        intArrayOf(6, 7, 8),
        intArrayOf(0, 3, 6),
        intArrayOf(1, 4, 7),
        intArrayOf(2, 5, 8),
        intArrayOf(0, 4, 8),
        intArrayOf(2, 4, 6),
    )

    fun cellToChar(v: Int): String = when (v) {
        1 -> "O"
        2 -> "X"
        else -> " "
    }

    /**
     * @return 0 表示無勝者；1 表示 O 勝；2 表示 X 勝
     */
    fun checkWinner(b: BoardStatus): Int {
        for (ln in LINES) {
            val a = b[ln[0]]
            if (a != 0 && a == b[ln[1]] && a == b[ln[2]]) return a
        }
        return 0
    }

    fun isDraw(movesAfterThisMove: Int, winner: Int): Boolean =
        winner == 0 && movesAfterThisMove >= 9
}
