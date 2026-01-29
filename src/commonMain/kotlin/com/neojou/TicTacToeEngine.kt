package com.neojou

import kotlin.random.Random

data class GameUpdate(
    val state: GameState,
    val logs: List<String> = emptyList()
)

object TicTacToeEngine {
    private val TAG = "TicTacToeEngine"

    /**
     * 產生初始 GameState
     * @param randomFirst 是否隨機決定先手（true=隨機 O 或 X 先，false=預設 O 先）
     * @return 初始 GameState（board 全空，turn=1 或 2，moves=0）
     */
    fun createInitialState(randomFirst: Boolean = true): GameState {
        val initialTurn = if (randomFirst) {
            if (Random.nextBoolean()) 1 else 2  // 50% O 先，50% X 先
        } else {
            1  // 預設 O 先
        }

        MyLog.add(TAG, "createInitialState: turn=$initialTurn (randomFirst=$randomFirst)", LogLevel.DEBUG)

        return GameState(
            board = BoardStatus(),
            turn = initialTurn,
            moves = 0,
            gameOver = false,
            iGameResult = 0,
            gameResult = null
        )
    }


    /**
     * @param aiPlayer 若為 null，則維持純雙人輪流；若非 null，輪到 X 時自動下子
     */
    fun onCellClick(prev: GameState, pos: Int, aiPlayer: AIPlayer? = null): GameUpdate {
    //    MyLog.add("onCellClick called: pos=$pos, turn=${prev.turn}, gameOver=${prev.gameOver}, aiPlayer=${aiPlayer != null}")

        // 若遊戲結束，不處理
        if (prev.gameOver) {
            //MyLog.add("Game already over, skip")
            return GameUpdate(prev)
        }

        // 若已啟用 AI，且現在輪到 X，忽略人類點擊（避免「搶下 X」）
        if (aiPlayer != null && prev.turn == 2) {
            MyLog.add(TAG, "AI turn (X), ignore human click", LogLevel.DEBUG)
            return GameUpdate(prev)
        }

        // 人類下
        val humanUpdate = applyMove(prev, pos, actor = "Mouse Click", aiPlayer = aiPlayer)
        if (humanUpdate.state == prev) {
            MyLog.add(TAG, "Invalid human move (pos occupied), skip", LogLevel.DEBUG)
            return humanUpdate
        }
        if (humanUpdate.state.gameOver) {
            //MyLog.add("Human move ends game")
            aiPlayer?.addLastMove(humanUpdate.state.board)
            return humanUpdate
        }

        // 若啟用 AI 且下一手輪到 X，讓 AI 走一步
        if (aiPlayer != null && humanUpdate.state.turn == 2) {
            MyLog.add(TAG, "AI turn after human move", LogLevel.DEBUG)
            //val aiPos = aiPlayer.chooseMove(humanUpdate.state.board)
            val aiPos = when (aiPlayer) {
                is QLearnAIPlayer -> aiPlayer.withEpsilon(0.0) { aiPlayer.chooseMove(humanUpdate.state.board) }
                else -> aiPlayer.chooseMove(humanUpdate.state.board)
            }

            if (aiPos != null && humanUpdate.state.board[aiPos] == 0) {
                val aiUpdate = applyMove(humanUpdate.state, aiPos, actor = "AI", aiPlayer = aiPlayer)
                //MyLog.add("AI chose pos=$aiPos")
                return GameUpdate(
                    state = aiUpdate.state,
                    logs = humanUpdate.logs + "AI chooses A[$aiPos]" + aiUpdate.logs
                )
            } else {
                MyLog.add(TAG, "AI no valid move or pos occupied", LogLevel.DEBUG)
            }
        }

        return humanUpdate
    }

    /**
     * 套用「當前回合」的一步落子 + 勝負判定。
     * 若 pos 無效（已有棋子），回傳 state 不變，並帶 log。
     */
    private fun applyMove(prev: GameState, pos: Int, actor: String, aiPlayer: AIPlayer? = null): GameUpdate {
        //MyLog.add("applyMove: actor=$actor, pos=$pos, turn=${prev.turn}")

        val cur = prev.board[pos]
        if (cur != 0) {
            MyLog.add(TAG, "Invalid move: pos $pos already occupied by ${TicTacToeRules.cellToChar(cur)}", LogLevel.DEBUG)
            return GameUpdate(
                prev,
                listOf("$actor on A[$pos], A[$pos] has been put ${TicTacToeRules.cellToChar(cur)} already")
            )
        }

        // 產生新 BoardStatus
        val nextBoard = BoardStatus(prev.board.copyArray())
        nextBoard.set(pos, prev.turn)

        val moves2 = prev.moves + 1
        val logs = mutableListOf<String>(
            "$actor on A[$pos] , A[$pos] is ${TicTacToeRules.cellToChar(prev.turn)}"
        )

        MyLog.add(TAG, "Move applied: board now = ${nextBoard.copyArray().contentToString()}", LogLevel.DEBUG)

        val winner = TicTacToeRules.checkWinner(nextBoard)
        if (winner == 1) {
            MyLog.add(TAG, "O wins!", LogLevel.DEBUG)
            val update = GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, iGameResult = 1, gameResult = "O is the winner"),
                logs + "O is the winner"
            )
            if (aiPlayer is QLearnAIPlayer) {
                val terminalReward = if (winner == aiPlayer.myType) 1.0 else -1.0
                aiPlayer.recordStepOutcome(terminalReward, nextBoard)
                MyLog.add(TAG, "Engine: Terminal reward recorded: $terminalReward (O wins)", LogLevel.DEBUG)
            }
            return update
        }
        if (winner == 2) {
            //MyLog.add("X wins!")
            val update = GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, iGameResult = 2, gameResult = "X is the winner"),
                logs + "X is the winner"
            )
            if (aiPlayer is QLearnAIPlayer) {
                val terminalReward = if (winner == aiPlayer.myType) 1.0 else -1.0
                aiPlayer.recordStepOutcome(terminalReward, nextBoard)
                //MyLog.add("Engine: Terminal reward recorded: $terminalReward (X wins)")
            }
            return update
        }

        if (TicTacToeRules.isDraw(moves2, winner)) {
            //MyLog.add("Draw!")
            val update = GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, iGameResult = 0, gameResult = "Draw"),
                logs + "Draw"
            )
            if (aiPlayer is QLearnAIPlayer) {
                aiPlayer.recordStepOutcome(0.0, nextBoard)
                //MyLog.add("Engine: Terminal reward recorded: 0.0 (Draw)")
            }
            return update
        }

        val nextTurn = if (prev.turn == 1) 2 else 1
        val update = GameUpdate(
            prev.copy(board = nextBoard, moves = moves2, turn = nextTurn),
            logs
        )

        // 中間步記錄（AI 下子時）
        if (aiPlayer is QLearnAIPlayer && prev.turn == aiPlayer.myType) {
            val reward = 0.0
            aiPlayer.recordStepOutcome(reward, nextBoard)
            //MyLog.add("Engine: AI mid-game transition recorded, pos=$pos, reward=0.0")
        }

        return update
    }


    /**
     * 如果 AI 先手，立即讓 AI 下第一步
     */
    fun aiFirstMove(initialState: GameState, aiPlayer: AIPlayer): GameUpdate {
        //MyLog.add("aiFirstMove called: turn=${initialState.turn}")

        if (initialState.turn != 2) {
            MyLog.add(TAG, "Not AI first turn, skip", LogLevel.DEBUG)
            return GameUpdate(initialState)
        }

        val aiPos = aiPlayer.chooseMove(initialState.board)
        if (aiPos == null || initialState.board[aiPos] != 0) {
            MyLog.add(TAG, "AI no valid first move or pos occupied", LogLevel.DEBUG)
            return GameUpdate(initialState)
        }

        //MyLog.add("AI first move: pos=$aiPos")
        return applyMove(initialState, aiPos, actor = "AI First Move", aiPlayer = aiPlayer)
    }

    fun simulateMoveWithResult(state: GameState, pos: Int, actor: AIPlayer? = null): GameState {
        if (state.gameOver) return state
        if (state.board[pos] != 0) return state

        val nextBoard = BoardStatus(state.board.copyArray())
        nextBoard.set(pos, state.turn)

        val moves2 = state.moves + 1
        val winner = TicTacToeRules.checkWinner(nextBoard)
        val isDraw = TicTacToeRules.isDraw(moves2, winner)
        val gameOver2 = (winner == 1 || winner == 2 || isDraw)

        // 讓「下這步的玩家」把 pending transition 補上 (r, s')
        if (actor is QLearnAIPlayer && state.turn == actor.myType) {
            val reward = when {
                !gameOver2 -> 0.0
                winner == actor.myType -> 1.0
                winner == 0 -> 0.0
                else -> -1.0
            }
            actor.recordStepOutcome(reward, nextBoard)
        }

        val nextTurn = if (state.turn == 1) 2 else 1
        return state.copy(
            board = nextBoard,
            moves = moves2,
            turn = if (gameOver2) state.turn else nextTurn,
            gameOver = gameOver2,
            iGameResult = if (gameOver2) winner else state.iGameResult,
            gameResult = if (gameOver2) when (winner) {
                1 -> "O is the winner"
                2 -> "X is the winner"
                else -> "Draw"
            } else null
        )
    }

}