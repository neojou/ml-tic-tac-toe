package com.neojou

import com.neojou.TicTacToeEngine
import kotlin.random.Random

object SelfPlaySandbox {

    /**
     * 運行自我對弈 N 盤，兩個 AI 共享 table 學習。
     * @param numGames 總盤數 (e.g., 100)
     * @param selfPlayRatio 自玩比例 (0.7 = 70% 自玩，30% vs random)
     * @param sharedTable 共享的 QSTable (外部注入)
     * @param onProgress 進度 callback (e.g., log "game i/100")
     * @return 統計：自玩勝率、vs random 勝率
     */
    fun runSelfPlay(
        numGames: Int,
        selfPlayRatio: Double = 0.7,  // 新增：自玩比例
        sharedTable: QSTable,
        onProgress: (completed: Int, stats: SelfPlayStats) -> Unit = { _, _ -> }
    ): SelfPlayStats {
        val aiO = QSTableAIPlayer(myType = 1, table = sharedTable)
        val aiX = QSTableAIPlayer(myType = 2, table = sharedTable)
        val aiNatureStupid = RandomAIPlayer()

        val numSelfPlay = (numGames * selfPlayRatio).toInt()
        val numVsRandom = numGames - numSelfPlay

        var selfPlayWins = 0
        var vsRandomWins = 0

        // 第一輪：自玩 (aiO vs aiX)
        repeat(numSelfPlay) { i ->
            aiO.resetForGame()
            aiX.resetForGame()

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
                if (pos == null) break
                state = TicTacToeEngine.simulateMove(state, pos)
            }

            // 學習：兩個 AI 各自 refine (共享 table)
            aiO.refine(state.iGameResult)
            aiX.refine(state.iGameResult)

            // 統計 (假設 iGameResult >0 為 aiO/X 贏；調整依需求)
            if (state.iGameResult > 0) selfPlayWins++

            onProgress(i + 1, SelfPlayStats(selfPlayWins, 0, numSelfPlay, 0))
        }

        // 第二輪：vs random (aiO vs aiNatureStupid)
        repeat(numVsRandom) { i ->
            aiO.resetForGame()

            var state = TicTacToeEngine.createInitialState(randomFirst = true)

            // 若 AI 先手 (turn==2)，立即下第一步 (random 先？調整)
            if (state.turn == 2) {
                val pos = aiNatureStupid.chooseMove(state.board)
                if (pos != null) {
                    state = TicTacToeEngine.simulateMove(state, pos)
                }
            }

            // 輪流下子直到結束
            while (!state.gameOver) {
                val currentPlayer = if (state.turn == 1) aiO else aiNatureStupid
                val pos = currentPlayer.chooseMove(state.board)
                if (pos == null) break
                state = TicTacToeEngine.simulateMove(state, pos)
            }

            aiO.refine(state.iGameResult)

            // 統計 (aiO 贏)
            if (state.iGameResult > 0) vsRandomWins++

            val totalCompleted = numSelfPlay + i + 1
            onProgress(totalCompleted, SelfPlayStats(selfPlayWins, vsRandomWins, numSelfPlay, numVsRandom))
        }

        return SelfPlayStats(selfPlayWins, vsRandomWins, numSelfPlay, numVsRandom)
    }

    data class SelfPlayStats(
        val selfPlayWins: Int,
        val vsRandomWins: Int,
        val numSelfPlay: Int,
        val numVsRandom: Int
    ) {
        val selfPlayWinRate = if (numSelfPlay > 0) selfPlayWins.toDouble() / numSelfPlay else 0.0
        val vsRandomWinRate = if (numVsRandom > 0) vsRandomWins.toDouble() / numVsRandom else 0.0
    }
}