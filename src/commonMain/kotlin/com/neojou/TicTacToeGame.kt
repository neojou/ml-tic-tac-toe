package com.neojou

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.random.Random
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay

private val TAG = "TicTacToeGame"

@Composable
fun TicTacToeGame(modifier: Modifier = Modifier) {

    var state by remember { mutableStateOf(GameState()) }
    var gameCount by remember { mutableStateOf(0) }  // 追蹤學習場數

    // val aiPlayer = remember { FirstEmptyWithRecordAIPlayer() }
    // val aiPlayer = remember { QSTableAIPlayer() }
    // val aiPlayer = remember { QSTableAIPlayer(myType = 2, table = sharedTable) }
    val brain = remember { QLearnBrain() }
    val aiPlayer = remember { QLearnAIPlayer(myType = 2, brain = brain) } // UI 用 X

    // NEW: coroutine scope (非阻塞 self-play)
    val scope = rememberCoroutineScope()

    /*
     *
     */
    fun newGame() {
        // 新增：隨機決定誰先手 (1=O 人先, 2=X AI 先)
        val initialState = TicTacToeEngine.createInitialState(randomFirst = true)
        state = initialState

        aiPlayer.resetForGame()

        // 如果 AI 先手 (turn==2)，立即讓 AI 下第一步
        if (initialState.turn == 2) {
            val aiFirstUpdate = TicTacToeEngine.aiFirstMove(initialState, aiPlayer)
            state = aiFirstUpdate.state
            aiFirstUpdate.logs.forEach { MyLog.add(TAG,it, LogLevel.DEBUG) }
        }
    }

    fun training_AI(loops: Int, gamesPerLoop: Int) {
        MyLog.add(TAG, "AI brain size before Training: ${aiPlayer.brain.size()}", LogLevel.DEBUG)
        val trainStats = SelfPlaySandbox.trainMixed(
            aiXFromUi = aiPlayer,
            loops,
            gamesPerLoop,   // 每個 loop 總訓練盤數；其中 80% self-play + 20% random
            selfPlayRatio = 0.8,
            onProgress = { _, _ -> } // 不逐局印
        )
        MyLog.add(TAG, "Training done - SelfPlay winRate: ${trainStats.selfPlayWinRate * 100}%", LogLevel.DEBUG)
    }

    fun onCellClick(pos: Int) {
        val update = TicTacToeEngine.onCellClick(state, pos, aiPlayer)
        update.logs.forEach { MyLog.add(TAG, it, LogLevel.DEBUG) }
        state = update.state

        if (state.gameOver) {
            aiPlayer.refine(state.iGameResult)

            scope.launch {
                training_AI(5, 300)
                delay(3000)
                newGame()
            }
        }
    }

    // 更新：Forget 函數，使用新方法清 QSTable
    fun onForget() {
        aiPlayer.resetForForget()
        gameCount = 0
        MyLog.add(TAG, "Forgot all records")
    }

    // Analyze 函數不變
    fun onAnalyze() {
        aiPlayer.showRecords()
        MyLog.add(TAG, "Analyzed records")
    }


    fun onGoHome() {

        scope.launch {
            training_AI(5, 500)

            val aiO = QLearnAIPlayer(myType = 1, brain = aiPlayer.brain)
            val eval = SelfPlaySandbox.evalVsRandomSummary(
                aiX = aiPlayer,
                aiO = aiO,
                gamesAsXSecond = 1000,
                gamesAsOFirst = 1000
            )
            MyLog.add(TAG, "Eval vs Random (epsilon=0): X second ${eval.winsAsXSecond}/${eval.gamesAsXSecond} (${eval.winRateAsXSecond * 100}%), " +
                    "O first ${eval.winsAsOFirst}/${eval.gamesAsOFirst} (${eval.winRateAsOFirst * 100}%), Overall ${eval.overallWinRate * 100}%", LogLevel.DEBUG)
            MyLog.add(TAG, "AI brain size after Training: ${aiPlayer.brain.size()}", LogLevel.DEBUG)

            gameCount += 5 * 500;
        }
    }

    val viewState = TicTacToePresenter.present(state)

    TicTacToeScreen(
        board = state.board,
        //viewState = viewState,
        modifier = modifier,
        onCellClick = ::onCellClick,
        //onNewGame = ::newGame,
        //onForget = ::onForget,
        //onAnalyze = ::onAnalyze,
        //onGoHome = ::onGoHome,  // NEW: 傳入
        //gameCount = gameCount
    )
}