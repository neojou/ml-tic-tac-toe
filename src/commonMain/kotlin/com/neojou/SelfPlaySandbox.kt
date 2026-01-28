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

// 第一輪：自玩（aiO vs aiX）
            val selfPlayEpisodes = AllEpisodes()
            repeat(eachTimes) { i ->
                val (finalState, aiWin, aiEpisode) = playGameCollectEpisodes(aiO, aiX, true)
                if (aiWin) selfPlayWins++

                // 自玩：加入 aiO 的 episode + myType
                selfPlayEpisodes.add(aiEpisode, finalState.iGameResult, aiO.myType)

                // 自玩時，也要加入 aiX 的 episode（因為雙方都要學）
                selfPlayEpisodes.add(aiX.episode, finalState.iGameResult, aiX.myType)

                val completed = (loopIndex * eachTimes * 3) + i + 1
                onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, eachTimes * loops, eachTimes * loops, eachTimes * loops))
            }
            selfPlayEpisodes.refineAll(sharedTable)


// 第二輪：AI 後手 vs random 先手（只輸才蒐集）
            val vsRandomAfterEpisodes = AllEpisodes()
            repeat(eachTimes) { i ->
                val (finalState, aiWin, aiEpisode) = playGameCollectEpisodes(aiO, aiNatureStupid, false, aiFirst = false)
                if (aiWin) vsRandomAfterWins++

                if (!aiWin) {
                //    vsRandomAfterEpisodes.add(aiEpisode, finalState.iGameResult, aiO.myType)
                }

                val completed = (loopIndex * eachTimes * 3) + eachTimes + i + 1
                onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, eachTimes * loops, eachTimes * loops, eachTimes * loops))
            }
            vsRandomAfterEpisodes.refineAll(sharedTable)

// 第三輪：AI 先手 vs random 後手（只輸才蒐集）
            val vsRandomFirstEpisodes = AllEpisodes()
            repeat(eachTimes) { i ->
                val (finalState, aiWin, aiEpisode) = playGameCollectEpisodes(aiO, aiNatureStupid, false, aiFirst = true)
                if (aiWin) vsRandomFirstWins++

                if (!aiWin) {
                //    vsRandomFirstEpisodes.add(aiEpisode, finalState.iGameResult, aiO.myType)
                }

                val completed = (loopIndex * eachTimes * 3) + eachTimes * 2 + i + 1
                onProgress(completed, SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, eachTimes * loops, eachTimes * loops, eachTimes * loops))
            }
            vsRandomFirstEpisodes.refineAll(sharedTable)

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

class AllEpisodes {
    private val episodes = mutableListOf<Triple<Episode, Int, Int>>()  // (episode, outcome, myType)

    fun add(episode: Episode, outcome: Int, myType: Int) {
        episodes.add(Triple(episode, outcome, myType))
    }

    fun clear() {
        episodes.clear()
    }

    fun refineAll(table: QSTable) {
        episodes.forEach { (episode, outcome, myType) ->
            episode.refine(table, outcome, myType = myType)  // 傳入 myType
            var current = episode
            repeat(3) {
                current = current.clockwise()
                current.refine(table, outcome, myType = myType)
            }
        }
        MyLog.add("Batch refine completed: ${episodes.size} episodes processed (myType-aware)")
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