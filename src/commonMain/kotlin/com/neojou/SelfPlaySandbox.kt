package com.neojou

import com.neojou.TicTacToeEngine
import kotlin.random.Random

object SelfPlaySandbox {

    /**
     * 運行自我對弈 N 盤，兩個 AI 共享 table 學習。
     * @param numGames 盤數 (e.g., 100)
     * @param sharedTable 共享的 QSTable (外部注入)
     * @param onProgress 進度 callback (e.g., log "game i/100")
     */
    fun runSelfPlay(
        numGames: Int,
        sharedTable: QSTable,
        onProgress: (completed: Int) -> Unit = {}
    ) {
        val aiO = QSTableAIPlayer(myType = 1, table = sharedTable)
        val aiX = QSTableAIPlayer(myType = 2, table = sharedTable)

        repeat(numGames) { i ->
            // 重置單局狀態 (保留 table)
            aiO.resetForGame()
            aiX.resetForGame()

            // 起始狀態 (隨機先手)
            var state = TicTacToeEngine.createInitialState(randomFirst = true)

            // 若 AI 先手 (turn==2)，立即下第一步
            if (state.turn == 2) {
                val pos = aiX.chooseMove(state.board)
                if (pos != null) {
                    state = TicTacToeEngine.simulateMove(state, pos)
                }
            }

            // 輪流下子直到結束
            while (!state.gameOver) {
                val currentAi = if (state.turn == 1) aiO else aiX
                val pos = currentAi.chooseMove(state.board)
                if (pos == null) break  // 異常，應不會發生
                state = TicTacToeEngine.simulateMove(state, pos)
            }

            // 學習：兩個 AI 各自 refine (共享 table，更新加倍但對稱)
            aiO.refine(state.iGameResult)
            aiX.refine(state.iGameResult)

            onProgress(i + 1)
        }
    }
}