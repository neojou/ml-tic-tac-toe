package com.neojou

object SelfPlaySandbox {

    fun trainSelfPlayOnly(
        aiXFromUi: QLearnAIPlayer,  // myType = 2
        loops: Int,
        eachTimes: Int,
        onProgress: (completed: Int, stats: SelfPlayStats) -> Unit = { _, _ -> }
    ): SelfPlayStats {

        // 用同一個 brain 建 O 外殼（myType=1）
        val aiO = QLearnAIPlayer(myType = 1, brain = aiXFromUi.brain)
        val aiX = aiXFromUi

        var oWins = 0
        val total = loops * eachTimes

        repeat(loops) { loopIndex ->
            repeat(eachTimes) { i ->
                // 這裡固定 O 先手：aiO vs aiX
                val (finalState, oWin) = playGame(
                    ai = aiO,
                    opponent = aiX,
                    isSelfPlay = true,
                    aiFirst = true
                )
                if (oWin) oWins++

                // 兩邊都 refine（各自 episodeTransitions；但共享 brain）
                aiO.refine(finalState.iGameResult)
                aiX.refine(finalState.iGameResult)

                val completed = loopIndex * eachTimes + i + 1
                onProgress(
                    completed,
                    SelfPlayStats(
                        selfPlayWins = oWins,
                        vsRandomAfterWins = 0,
                        vsRandomFirstWins = 0,
                        numSelfPlay = completed,
                        numVsRandomAfter = 0,
                        numVsRandomFirst = 0
                    )
                )
            }
        }

        return SelfPlayStats(
            selfPlayWins = oWins,
            vsRandomAfterWins = 0,
            vsRandomFirstWins = 0,
            numSelfPlay = total,
            numVsRandomAfter = 0,
            numVsRandomFirst = 0
        )
    }

    private fun playGame(
        ai: QLearnAIPlayer,
        opponent: AIPlayer,
        isSelfPlay: Boolean,
        aiFirst: Boolean = true
    ): Pair<GameState, Boolean> {
        ai.resetForGame()
        if (isSelfPlay) (opponent as? QLearnAIPlayer)?.resetForGame()

        var state = if (aiFirst) {
            GameState(turn = ai.myType)
        } else {
            GameState(turn = if (ai.myType == 1) 2 else 1)
        }

        // 第一手也要把 actor 傳進 simulateMoveWithResult，才能正確補 transition
        val firstPlayer = if (aiFirst) ai else opponent
        val firstPos = firstPlayer.chooseMove(state.board)
        if (firstPos != null) {
            state = TicTacToeEngine.simulateMoveWithResult(state, firstPos, firstPlayer)
        }

        while (!state.gameOver) {
            val currentPlayer = if (state.turn == ai.myType) ai else opponent
            val pos = currentPlayer.chooseMove(state.board) ?: break
            state = TicTacToeEngine.simulateMoveWithResult(state, pos, currentPlayer)
        }

        val aiWin = state.iGameResult == ai.myType
        return state to aiWin
    }

    data class EvalVsRandomResult(
        val winsAsXSecond: Int,
        val gamesAsXSecond: Int,
        val winsAsOFirst: Int,
        val gamesAsOFirst: Int
    ) {
        val winRateAsXSecond = if (gamesAsXSecond > 0) winsAsXSecond.toDouble() / gamesAsXSecond else 0.0
        val winRateAsOFirst = if (gamesAsOFirst > 0) winsAsOFirst.toDouble() / gamesAsOFirst else 0.0
        val overallWinRate =
            if (gamesAsXSecond + gamesAsOFirst > 0)
                (winsAsXSecond + winsAsOFirst).toDouble() / (gamesAsXSecond + gamesAsOFirst)
            else 0.0
    }

    fun evalVsRandomSummary(
        aiX: QLearnAIPlayer,     // myType=2
        aiO: QLearnAIPlayer,     // myType=1 (共享同 brain)
        gamesAsXSecond: Int,
        gamesAsOFirst: Int
    ): EvalVsRandomResult {
        val randomAI = RandomAIPlayer()

        // 1) X 後手評估：aiX vs random(O先手)
        val winsAsXSecond = aiX.withEpsilon(0.0) {
            var wins = 0
            repeat(gamesAsXSecond) {
                val (_, xWin) = playGame(ai = aiX, opponent = randomAI, isSelfPlay = false, aiFirst = false)
                if (xWin) wins++
            }
            wins
        }

        // 2) O 先手評估：aiO vs random(X後手)
        val winsAsOFirst = aiO.withEpsilon(0.0) {
            var wins = 0
            repeat(gamesAsOFirst) {
                val (_, oWin) = playGame(ai = aiO, opponent = randomAI, isSelfPlay = false, aiFirst = true)
                if (oWin) wins++
            }
            wins
        }

        return EvalVsRandomResult(
            winsAsXSecond = winsAsXSecond,
            gamesAsXSecond = gamesAsXSecond,
            winsAsOFirst = winsAsOFirst,
            gamesAsOFirst = gamesAsOFirst
        )
    }

    fun trainMixed(
        aiXFromUi: QLearnAIPlayer,   // myType = 2
        loops: Int,
        gamesPerLoop: Int,
        selfPlayRatio: Double = 0.8, // 80%
        onProgress: (completed: Int, stats: SelfPlayStats) -> Unit = { _, _ -> }
    ): SelfPlayStats {

        val aiX = aiXFromUi
        val aiO = QLearnAIPlayer(myType = 1, brain = aiX.brain)

        var selfPlayWins = 0
        var vsRandomAfterWins = 0
        var vsRandomFirstWins = 0

        var doneSelf = 0
        var doneAfter = 0
        var doneFirst = 0

        val randomAI = RandomAIPlayer()

        val selfN = (gamesPerLoop * selfPlayRatio).toInt().coerceIn(0, gamesPerLoop)
        val randN = gamesPerLoop - selfN

        repeat(loops) { loopIndex ->

            // A) self-play 訓練：交替先手，讓 O/X 都有當先手與後手的經驗
            repeat(selfN) { i ->
                val oFirst = (i % 2 == 0)
                val (finalState, aiWin) = if (oFirst) {
                    playGame(ai = aiO, opponent = aiX, isSelfPlay = true, aiFirst = true)   // O 先手
                } else {
                    playGame(ai = aiX, opponent = aiO, isSelfPlay = true, aiFirst = true)   // X 先手
                }

                // 這裡的 selfPlayWins：只是示範用「先手那個 ai 的勝場」；你也可以改成統計 X 勝場/或 O 勝場/或非輸率
                if (aiWin) selfPlayWins++
                doneSelf++

                aiO.refine(finalState.iGameResult)
                aiX.refine(finalState.iGameResult)

                // 探索率一起衰減，避免兩個外殼的 epsilon 漂太開
                aiO.decayEpsilon()
                aiX.decayEpsilon()

                val completed = loopIndex * gamesPerLoop + doneSelf + doneAfter + doneFirst
                onProgress(
                    completed,
                    SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, doneSelf, doneAfter, doneFirst)
                )
            }

            // B) vs Random 訓練：把 20% 再切一半，覆蓋「X 後手」與「O 先手」
            repeat(randN) { i ->
                val trainXSecond = (i % 2 == 0)

                val (finalState, aiWin) = if (trainXSecond) {
                    // X 後手 vs random(O 先手)
                    playGame(ai = aiX, opponent = randomAI, isSelfPlay = false, aiFirst = false)
                } else {
                    // O 先手 vs random(X 後手)
                    playGame(ai = aiO, opponent = randomAI, isSelfPlay = false, aiFirst = true)
                }

                if (trainXSecond) {
                    if (aiWin) vsRandomAfterWins++
                    doneAfter++
                } else {
                    if (aiWin) vsRandomFirstWins++
                    doneFirst++
                }

                // 訓練階段：要 refine（把 vs random 的資料也吃進來）
                aiO.refine(finalState.iGameResult)
                aiX.refine(finalState.iGameResult)
                aiO.decayEpsilon()
                aiX.decayEpsilon()

                val completed = loopIndex * gamesPerLoop + doneSelf + doneAfter + doneFirst
                onProgress(
                    completed,
                    SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, doneSelf, doneAfter, doneFirst)
                )
            }
        }

        return SelfPlayStats(selfPlayWins, vsRandomAfterWins, vsRandomFirstWins, doneSelf, doneAfter, doneFirst)
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

    val overallVsRandomWinRate =
        if (numVsRandomAfter + numVsRandomFirst > 0)
            (vsRandomAfterWins + vsRandomFirstWins).toDouble() / (numVsRandomAfter + numVsRandomFirst)
        else 0.0
}
