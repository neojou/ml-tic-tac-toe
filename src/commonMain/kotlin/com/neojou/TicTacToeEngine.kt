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

        // 人類下
        val humanUpdate = applyMove(prev, pos, actor = "Mouse Click")
        if (humanUpdate.state == prev) return humanUpdate
        if (humanUpdate.state.gameOver) {
            aiPlayer?.addLastMove(humanUpdate.state.board)
            return humanUpdate
        }

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
     * 新增：產生初始 GameState，隨機決定誰先手 (1=O 人先, 2=X AI 先)
     */
    fun createInitialState(randomFirst: Boolean = true): GameState {
        val initialTurn = if (!randomFirst) 2 else (1..2).random()  // 預設 X 先，可關閉隨機
        return GameState(turn = initialTurn)
    }


    // NEW: 靜默套用一步 (無 log/actor，用於 sandbox)
    fun simulateMove(prev: GameState, pos: Int): GameState {
        val cur = prev.board[pos]
        if (cur != 0) return prev  // 無效，狀態不變

        val nextBoard = BoardStatus(prev.board.copyArray())
        nextBoard.set(pos, prev.turn)

        val moves2 = prev.moves + 1

        val winner = TicTacToeRules.checkWinner(nextBoard)
        if (winner == prev.turn) {
            return prev.copy(
                board = nextBoard,
                moves = moves2,
                gameOver = true,
                iGameResult = prev.turn,
                gameResult = if (prev.turn == 1) "O is the winner" else "X is the winner"
            )
        }

        if (TicTacToeRules.isDraw(moves2, winner)) {
            return prev.copy(
                board = nextBoard,
                moves = moves2,
                gameOver = true,
                iGameResult = 0,
                gameResult = "Draw"
            )
        }

        val nextTurn = if (prev.turn == 1) 2 else 1
        return prev.copy(board = nextBoard, moves = moves2, turn = nextTurn)
    }

    /**
     * 新增：如果 AI 先手，立即讓 AI 下第一步 (用於 newGame)
     */

    // MOD: aiFirstMove 內部改用 simulateMove (但保留 logs；sandbox 不需此)
    fun aiFirstMove(initialState: GameState, aiPlayer: AIPlayer): GameUpdate {
        if (initialState.turn != 2) return GameUpdate(initialState)

        val aiPos = aiPlayer.chooseMove(initialState.board)
        if (aiPos == null || initialState.board[aiPos] != 0) return GameUpdate(initialState)

        // MOD: 用 simulateMove 計算新狀態，再包 logs
        val newState = simulateMove(initialState, aiPos)
        val logs = listOf("AI First Move on A[$aiPos], A[$aiPos] is ${TicTacToeRules.cellToChar(2)}")
        return GameUpdate(newState, logs)
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
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, iGameResult = 1, gameResult = "O is the winner"),
                logs + "O is the winner"
            )
        }
        if (winner == 2) {
            return GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, iGameResult = 2, gameResult = "X is the winner"),
                logs + "X is the winner"
            )
        }

        if (TicTacToeRules.isDraw(moves2, winner)) {
            return GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, iGameResult = 0, gameResult = "Draw"),
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