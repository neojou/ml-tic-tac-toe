package com.neojou

/**
 * BoardStatus cell values:
 * 0 = empty, 1/2 = marks (O/X or X/O depending on your existing convention)
 */
object BoardAnalyze {

    /**
     * 從 SA -> SB 推回「剛剛下在哪一格 pos」。
     * 規則：必須恰好只有一格從 0 變成 1 或 2，其餘格完全相同。
     *
     * @return pos (0..8)
     * @throws IllegalArgumentException 若 SA->SB 不是合法的一步
     */
    fun getPos(sa: BoardStatus, sb: BoardStatus): Int {
        var found: Int? = null
        for (i in 0..8) {
            val a = sa[i]
            val b = sb[i]
            if (a != b) {
                require(a == 0 && b in 1..2) { "Invalid transition at pos=$i: $a -> $b" }
                require(found == null) { "More than one move found (already at pos=$found, another at pos=$i)" }
                found = i
            }
        }
        return requireNotNull(found) { "No move found (boards are identical)" }
    }

    /**
     * 回傳目前所有可下的空白位置集合。
     */
    fun getEmptyPosSet(board: BoardStatus): Set<Int> {
        val s = LinkedHashSet<Int>()
        for (i in 0..8) {
            if (board[i] == 0) s.add(i)
        }
        return s
    }

    /**
     * 從 SI 狀態在 pos 落下 type(1/2) 後，產生新狀態 SO（不修改 SI）。
     *
     * @param type 只能是 1 或 2（對應 O/X；沿用你專案既有對應）
     */
    fun newBoardStatus(si: BoardStatus, pos: Int, type: Int): BoardStatus {
        require(pos in 0..8) { "pos must be in 0..8" }
        require(type in 1..2) { "type must be 1 or 2" }
        require(si[pos] == 0) { "pos=$pos is not empty" }

        val a = si.copyArray()
        a[pos] = type
        return BoardStatus(a)
    }

    // 如果你更想用語意化型別，也可選擇用這個 overload：
    enum class Mark(val value: Int) { O(1), X(2) }

    fun newBoardStatus(si: BoardStatus, pos: Int, mark: Mark): BoardStatus =
        newBoardStatus(si, pos, mark.value)

// BoardAnalyze.kt 新增

    /**
     * 判斷在這個狀態下，如果下在 pos，是否會阻擋對方即將完成的連線（即對方有兩個同色且空一格的威脅）
     * 返回 true 表示這步是「關鍵阻擋」
     */
    fun isBlockingThreat(board: BoardStatus, pos: Int, myType: Int): Boolean {
        if (board[pos] != 0) return false
        val opponent = if (myType == 1) 2 else 1

        // 模擬下這步
        val tempBoard = BoardAnalyze.newBoardStatus(board, pos, myType)

        // 檢查對方是否還有「兩個同色 + 空一格」的威脅（即下完後對方無法立即贏）
        // 這裡簡化：檢查所有可能的三連線，如果原本有威脅且現在被擋
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // 橫
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // 豎
            listOf(0, 4, 8), listOf(2, 4, 6)  // 斜
        )

        for (line in lines) {
            val marks = line.map { board[it] }
            val emptyCount = marks.count { it == 0 }
            val oppCount = marks.count { it == opponent }
            if (oppCount == 2 && emptyCount == 1) {
                // 原本有威脅
                val newMarks = line.map { tempBoard[it] }
                if (newMarks.count { it == opponent } == 2 && newMarks.count { it == 0 } == 0) {
                    return true  // 下完後威脅消失
                }
            }
        }
        return false
    }

    /**
     * 判斷下 pos 是否創造「兩個同色 + 空一格」的威脅（即自己即將贏）
     */
    fun isCreatingThreat(board: BoardStatus, pos: Int, myType: Int): Boolean {
        if (board[pos] != 0) return false

        val tempBoard = BoardAnalyze.newBoardStatus(board, pos, myType)

        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )

        for (line in lines) {
            val marks = line.map { tempBoard[it] }
            if (marks.count { it == myType } == 2 && marks.count { it == 0 } == 1) {
                return true
            }
        }
        return false
    }

}