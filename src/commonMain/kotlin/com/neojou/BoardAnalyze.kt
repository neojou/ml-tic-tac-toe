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
}
