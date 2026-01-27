package com.neojou

/**
 * 繼承 FirstEmptyAIPlayer：預設仍使用 super.chooseMove(board) (第一個空格)
 * 但額外做 tabular policy learning 前身的紀錄。
 */
class FirstEmptyWithRecordAIPlayer(
    private val myType: Int = 2, // 你的 X/O 對應沿用專案慣例；BoardStatus cell 允許 1..2
) : FirstEmptyAIPlayer() {

    val table = QSTable()
    val episode = Episode()

    // S0：全空盤面（第一次 chooseMove 用）
    private val emptyS0 = BoardStatus(IntArray(9) { 0 })

    // S2, S4...：上次我下完後的盤面
    private var lastAfterMyMove: BoardStatus? = null

    private fun snapshot(s: BoardStatus): BoardStatus =
        BoardStatus(s.copyArray())

    override fun chooseMove(board: BoardStatus): Int? {

        // --- (A) 記錄「對手剛剛那一步」：S_prev -> board ---
        val sPrev = lastAfterMyMove ?: emptyS0
        val oppPos = tryGetSingleMovePosOrNull(sPrev, board)

        if (oppPos != null) {
            val saSnap = snapshot(sPrev)
            val legal = BoardAnalyze.getEmptyPosSet(sPrev)

            table.ensureBuilt(saSnap, legal, defaultWeight = 1)
            table.markChosen(saSnap, oppPos, chosenWeight = 2)

            episode.append(saSnap, oppPos, legal, playerType = null)
        }

        // --- (B) 用原本策略選我方落子（第一個空格）---
        val myPos = super.chooseMove(board) ?: return null

        // --- (C) 記錄「我方這一步」：board 選 myPos ---
        val saSnap2 = snapshot(board)
        val legal2 = BoardAnalyze.getEmptyPosSet(board)

        table.ensureBuilt(saSnap2, legal2, defaultWeight = 1)
        table.markChosen(saSnap2, myPos, chosenWeight = 2)

        episode.append(saSnap2, myPos, legal2, playerType = myType)

        // --- (D) 更新 lastAfterMyMove ---
        lastAfterMyMove = BoardAnalyze.newBoardStatus(board, myPos, myType)
        return myPos
    }

    fun resetEpisode() {
        table.clear()
        episode.clear()
        lastAfterMyMove = null
    }

    private fun tryGetSingleMovePosOrNull(sa: BoardStatus, sb: BoardStatus): Int? {
        return try {
            BoardAnalyze.getPos(sa, sb)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override fun showRecords() {
        table.show("QSTable")
        episode.show(table, "Episode")
    }

    override fun addLastMove(board: BoardStatus) {
        val sPrev = lastAfterMyMove ?: emptyS0
        val oppPos = tryGetSingleMovePosOrNull(sPrev, board) ?: return

        val saSnap = snapshot(sPrev)
        val legal = BoardAnalyze.getEmptyPosSet(sPrev)

        table.ensureBuilt(saSnap, legal, defaultWeight = 1)
        table.markChosen(saSnap, oppPos, chosenWeight = 2)

        episode.append(saSnap, oppPos, legal, playerType = null)

        // 標記「已經補記到這個盤面」，避免外部重複呼叫 addLastMove 時再加一次
        lastAfterMyMove = snapshot(board)
    }
}
