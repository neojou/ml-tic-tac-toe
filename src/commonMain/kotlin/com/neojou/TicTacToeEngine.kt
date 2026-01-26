package com.neojou

data class GameUpdate(
    val state: GameState,
    val logs: List<String> = emptyList()
)

object TicTacToeEngine {

    /**
     * @param aiPlayer 若為 null，則維持純雙人輪流；若非 null，輪到 X 時自動下子
     */
    fun onCellClick(prev: GameState, pos: Int, aiPlayer: AIPlayer? = null): GameUpdate {
        // 若遊戲結束，不處理
        if (prev.gameOver) return GameUpdate(prev)

        // 若已啟用 AI，且現在輪到 X，忽略人類點擊（避免「搶下 X」）
        if (aiPlayer != null && prev.turn == 2) return GameUpdate(prev)

        // 先套用人類這一步
        val humanUpdate = applyMove(prev, pos, actor = "Mouse Click")
        if (humanUpdate.state == prev) return humanUpdate // 無效點擊（點到已有棋子等）
        if (humanUpdate.state.gameOver) return humanUpdate

        // 若啟用 AI 且下一手輪到 X，讓 AI 走一步
        if (aiPlayer != null && humanUpdate.state.turn == 2) {
            val aiPos = aiPlayer.chooseMove(humanUpdate.state.board)
            if (aiPos != null && humanUpdate.state.board[aiPos] == 0) {
                val aiUpdate = applyMove(humanUpdate.state, aiPos, actor = "AI")
                return GameUpdate(
                    state = aiUpdate.state,
                    logs = humanUpdate.logs + "AI chooses A[$aiPos]" + aiUpdate.logs
                )
            }
        }

        return humanUpdate
    }

    /**
     * 套用「當前回合」的一步落子 + 勝負判定。
     * 若 pos 無效（已有棋子），回傳 state 不變，並帶 log。
     */
    private fun applyMove(prev: GameState, pos: Int, actor: String): GameUpdate {
        val cur = prev.board[pos]
        if (cur != 0) {
            return GameUpdate(
                prev,
                listOf("$actor on A[$pos], A[$pos] has been put ${TicTacToeRules.cellToChar(cur)} already")
            )
        }

        // 產生新 BoardStatus（確保 state 以「新物件」更新，重組才穩）
        val nextBoard = BoardStatus(prev.board.copyArray())
        nextBoard.set(pos, prev.turn)

        val moves2 = prev.moves + 1
        val logs = mutableListOf<String>(
            "$actor on A[$pos] , A[$pos] is ${TicTacToeRules.cellToChar(prev.turn)}"
        )

        val winner = TicTacToeRules.checkWinner(nextBoard)
        if (winner == 1) {
            return GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, gameResult = "O is the winner"),
                logs + "O is the winner"
            )
        }
        if (winner == 2) {
            return GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, gameResult = "X is the winner"),
                logs + "X is the winner"
            )
        }

        if (TicTacToeRules.isDraw(moves2, winner)) {
            return GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, gameResult = "Draw"),
                logs + "Draw"
            )
        }

        val nextTurn = if (prev.turn == 1) 2 else 1
        return GameUpdate(
            prev.copy(board = nextBoard, moves = moves2, turn = nextTurn),
            logs
        )
    }
}
