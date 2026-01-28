package com.neojou

import com.neojou.TicTacToeEngine
import kotlin.random.Random

object SelfPlaySandbox {

    fun runSelfPlay(
        loops: Int,
        eachTimes: Int,
        sharedTable: QSTable,
        onProgress: (completed: Int, stats: SelfPlayStats) -> Unit = { _, _ -> }
    ): SelfPlayStats {
        val aiO = QSTableAIPlayer(myType = 1, table = sharedTable)
        val aiX = QSTableAIPlayer(myType = 2, table = sharedTable)
        val aiNatureStupid = RandomAIPlayer()

        var selfPlayWins = 0
        var vsRandomAfterWins = 0
        var vsRandomFirstWins = 0

        val totalGames = loops * eachTimes * 3

        repeat(loops) { loopIndex ->
            MyLog.add("Starting loop ${loopIndex + 1}/$loops (eachTimes=$eachTimes)")

            // 第一輪：自玩（全部蒐集並 refine）
            val allEpisodes = AllEpisodes()
            repeat(eachTimes) { i ->
                val (finalState, aiWin, aiEpisode) = playGameCollectEpisodes(aiO, aiX, true)
                if (aiWin) selfPlayWins++

                // 自玩：全部加入（無論輸贏）
                allEpisodes.add(aiEpisode, finalState.iGameResult)

                val completed = (loopIndex * eachTimes * 3) + i + 1
                onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, eachTimes * loops, eachTimes * loops, eachTimes * loops))
            }

            // 第二輪：AI 後手 vs random 先手（只輸才蒐集）
            repeat(eachTimes) { i ->
                val (finalState, aiWin, aiEpisode) = playGameCollectEpisodes(aiO, aiNatureStupid, false, aiFirst = false)
                if (aiWin) vsRandomAfterWins++

                // 只在 AI 輸時加入 episode（aiWin == false）
                if (!aiWin) {
                   allEpisodes.add(aiEpisode, finalState.iGameResult)
                }

                val completed = (loopIndex * eachTimes * 3) + eachTimes + i + 1
                onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, eachTimes * loops, eachTimes * loops, eachTimes * loops))
            }

            // 第三輪：AI 先手 vs random 後手（只輸才蒐集）
            repeat(eachTimes) { i ->
                val (finalState, aiWin, aiEpisode) = playGameCollectEpisodes(aiO, aiNatureStupid, false, aiFirst = true)
                if (aiWin) vsRandomFirstWins++

                // 只在 AI 輸時加入 episode
                if (!aiWin) {
                    allEpisodes.add(aiEpisode, finalState.iGameResult)
                }

                val completed = (loopIndex * eachTimes * 3) + eachTimes * 2 + i + 1
                onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, eachTimes * loops, eachTimes * loops, eachTimes * loops))
            }
            allEpisodes.refineAll(sharedTable)

            MyLog.add("Loop ${loopIndex + 1} finished. Current stats: SelfPlay ${selfPlayWins.toDouble() / (eachTimes * (loopIndex + 1)) * 100}%, " +
                    "VsRandom After ${vsRandomAfterWins.toDouble() / (eachTimes * (loopIndex + 1)) * 100}%, " +
                    "VsRandom First ${vsRandomFirstWins.toDouble() / (eachTimes * (loopIndex + 1)) * 100}%")
        }

        return SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, eachTimes * loops, eachTimes * loops, eachTimes * loops)
    }

    // playGameCollectEpisodes 保持原樣（回傳 AI 的 episode）
    private fun playGameCollectEpisodes(
        ai: QSTableAIPlayer,
        opponent: AIPlayer,
        isSelfPlay: Boolean,
        aiFirst: Boolean = true
    ): Triple<GameState, Boolean, Episode> {
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
        val aiEpisode = ai.episode

        return Triple(state, aiWin, aiEpisode)
    }
}

// AllEpisodes 和 SelfPlayStats 保持不變
class AllEpisodes {
    private val episodes = mutableListOf<Pair<Episode, Int>>()

    fun add(episode: Episode, outcome: Int) {
        episodes.add(episode to outcome)
    }

    fun clear() {
        episodes.clear()
    }

    fun refineAll(table: QSTable) {
        episodes.forEach { (episode, outcome) ->
            episode.refine(table, outcome)
            var current = episode
            repeat(3) {
                current = current.clockwise()
                current.refine(table, outcome)
            }
        }
        MyLog.add("Batch refine completed: ${episodes.size} episodes processed (only losses for vs random)")
    }
}

// SelfPlayStats 不變
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