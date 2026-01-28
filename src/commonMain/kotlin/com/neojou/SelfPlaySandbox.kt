package com.neojou

import com.neojou.TicTacToeEngine
import kotlin.random.Random

object SelfPlaySandbox {

    /**
     * 運行自我對弈與對隨機對手的對局，每種模式固定跑 numGames 次。
     * 總盤數 = 3 × numGames（自玩 + AI後手vs隨機 + AI先手vs隨機）
     */
    fun runSelfPlay(
        numGames: Int,
        sharedTable: QSTable,
        onProgress: (completed: Int, stats: SelfPlayStats) -> Unit = { _, _ -> }
    ): SelfPlayStats {
        val aiO = QSTableAIPlayer(myType = 1, table = sharedTable)
        val aiX = QSTableAIPlayer(myType = 2, table = sharedTable)
        val aiNatureStupid = RandomAIPlayer()

        var selfPlayWins = 0
        var vsRandomAfterWins = 0   // AI 後手 vs random 先手
        var vsRandomFirstWins = 0   // AI 先手 vs random 後手

        val totalGames = numGames * 3

        // 第一輪：自玩 (aiO vs aiX)
        repeat(numGames) { i ->
            val (finalState, aiWin) = playGame(aiO, aiX, true)
            if (aiWin) selfPlayWins++

            aiO.refine(finalState.iGameResult)
            aiX.refine(finalState.iGameResult)

            val completed = i + 1
            onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, numGames, numGames, numGames))
        }

        // 第二輪：AI 後手 vs random 先手
        repeat(numGames) { i ->
            val (finalState, aiWin) = playGame(aiO, aiNatureStupid, false, aiFirst = false)
            if (aiWin) vsRandomAfterWins++

            aiO.refine(finalState.iGameResult)

            val completed = numGames + i + 1
            onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, numGames, numGames, numGames))
        }

        // 第三輪：AI 先手 vs random 後手
        repeat(numGames) { i ->
            val (finalState, aiWin) = playGame(aiO, aiNatureStupid, false, aiFirst = true)
            if (aiWin) vsRandomFirstWins++

            aiO.refine(finalState.iGameResult)

            val completed = numGames * 2 + i + 1
            onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, numGames, numGames, numGames))
        }

        return SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, numGames, numGames, numGames)
    }

    // playGame 保持不變（您的原版）
    private fun playGame(
        ai: QSTableAIPlayer,
        opponent: AIPlayer,
        isSelfPlay: Boolean,
        aiFirst: Boolean = true
    ): Pair<GameState, Boolean> {
        ai.resetForGame()
        if (isSelfPlay) (opponent as QSTableAIPlayer).resetForGame()

        var state = if (aiFirst) {
            GameState(turn = ai.myType)
        } else {
            GameState(turn = if (ai.myType == 1) 2 else 1)
        }

        val firstPlayer = if (aiFirst) ai else opponent
        val pos = firstPlayer.chooseMove(state.board)
        if (pos != null) {
            state = TicTacToeEngine.simulateMove(state, pos)
        }

        while (!state.gameOver) {
            val currentPlayer = if (state.turn == ai.myType) ai else opponent
            val pos = currentPlayer.chooseMove(state.board)
            if (pos == null) break
            state = TicTacToeEngine.simulateMove(state, pos)
        }

        val aiWin = state.iGameResult == ai.myType

        if (isSelfPlay) {
            (opponent as QSTableAIPlayer).refine(state.iGameResult)
        }

        return state to aiWin
    }
}

data class SelfPlayStats(
    val selfPlayWins: Int,
    val vsRandomAfterWins: Int,
    val vsRandomFirstWins: Int,
    val numSelfPlay: Int,
    val numVsRandomAfter: Int,
    val numVsRandomFirst: Int
) {
    val selfPlayWinRate = if (numSelfPlay > 0) selfPlayWins.toDouble() / numSelfPlay else 0.0
    val vsRandomAfterWinRate = if (numVsRandomAfter > 0) vsRandomAfterWins.toDouble() / numVsRandomAfter else 0.0
    val vsRandomFirstWinRate = if (numVsRandomFirst > 0) vsRandomFirstWins.toDouble() / numVsRandomFirst else 0.0
    val overallVsRandomWinRate = (vsRandomAfterWins + vsRandomFirstWins).toDouble() / (numVsRandomAfter + numVsRandomFirst)
}