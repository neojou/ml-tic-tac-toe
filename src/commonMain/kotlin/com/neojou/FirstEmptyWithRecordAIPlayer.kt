package com.neojou

/**
 * 繼承 FirstEmptyAIPlayer：預設仍使用 super.chooseMove(board) (第一個空格) [file:4]
 * 但額外做 tabular policy learning 前身的紀錄。
 */
class FirstEmptyWithRecordAIPlayer(
    private val myType: Int = 2, // 你的 X/O 對應沿用專案慣例；BoardStatus cell 允許 1..2 [file:2]
) : FirstEmptyAIPlayer() {

    val table = RecordTable()
    val episode = Episode()

    private var roundCounter = 0

    // S0：全空盤面（第一次 chooseMove 用）
    private val emptyS0 = BoardStatus(IntArray(9) { 0 })

    // S2, S4...：上次我下完後的盤面
    private var lastAfterMyMove: BoardStatus? = null

    private fun snapshot(s: BoardStatus): BoardStatus = BoardStatus(s.copyArray()) // [file:2]

    override fun chooseMove(board: BoardStatus): Int? {
        // --- (A) 記錄「對手剛剛那一步」：S_prev -> board ---
        val sPrev = lastAfterMyMove ?: emptyS0

        // 若盤面不同才視為「對手下了一步」；若你允許 AI 先手，第一次可能 sPrev==board
        val oppPos = tryGetSingleMovePosOrNull(sPrev, board)
        if (oppPos != null) {
            val saSnap = snapshot(sPrev)
            val chosenOppRow = table.add(
                round = ++roundCounter,
                sa = saSnap,
                pos = oppPos,
                score = 2
            )
            episode.append(chosenOppRow)

            // 同 round、同 SA：其餘可下空格都記 1 分
            for (p in BoardAnalyze.getEmptyPosSet(sPrev)) {
                if (p != oppPos) table.add(roundCounter, saSnap, p, 1)
            }
        }

        // --- (B) 用原本策略選我方落子（第一個空格）---
        val myPos = super.chooseMove(board) // FirstEmptyAIPlayer: 由 0..8 找第一個空格 [file:4]
        if (myPos == null) return null

        // --- (C) 記錄「我方這一步」：board(S1/S3) 選 myPos ---
        val saSnap2 = snapshot(board)
        val chosenMyRow = table.add(
            round = ++roundCounter,
            sa = saSnap2,
            pos = myPos,
            score = 2
        )
        episode.append(chosenMyRow)

        // 同 round、同 SA：其餘可下空格都記 1 分
        for (p in BoardAnalyze.getEmptyPosSet(board)) {
            if (p != myPos) table.add(roundCounter, saSnap2, p, 1)
        }

        // --- (D) 先記錄下完後盤面：S2 = newBoardStatus(S1, myPos, X) ---
        lastAfterMyMove = BoardAnalyze.newBoardStatus(board, myPos, myType)

        return myPos
    }

    fun resetEpisode() {
        table.clear()
        episode.clear()
        roundCounter = 0
        lastAfterMyMove = null
    }

    private fun tryGetSingleMovePosOrNull(sa: BoardStatus, sb: BoardStatus): Int? {
        // BoardAnalyze.getPos 會要求「恰好一格從 0 -> 1/2」，否則丟例外
        return try {
            BoardAnalyze.getPos(sa, sb)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override fun showRecords() {
        table.show("RecordTable")
        episode.show("Episode")
    }

    override fun addLastMove(board: BoardStatus) {
        val sPrev = lastAfterMyMove ?: emptyS0

        // 若最後盤面相對於 sPrev 沒有「恰好一步」可推回，就不補記（避免重複或不合法盤面）
        val oppPos = tryGetSingleMovePosOrNull(sPrev, board) ?: return

        val saSnap = snapshot(sPrev)
        val chosenOppRow = table.add(
            round = ++roundCounter,
            sa = saSnap,
            pos = oppPos,
            score = 2
        )
        episode.append(chosenOppRow)

        // 同 round、同 SA：其餘可下空格都記 1 分
        for (p in BoardAnalyze.getEmptyPosSet(sPrev)) {
            if (p != oppPos) table.add(roundCounter, saSnap, p, 1)
        }

        // 標記「已經補記到這個盤面」，避免外部重複呼叫 addLastMove 時再加一次
        lastAfterMyMove = snapshot(board)
    }

}
