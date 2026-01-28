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

@Composable
fun TicTacToeGame(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(GameState()) }
    var gameCount by remember { mutableStateOf(0) }  // 追蹤學習場數

    // NEW: 共享 table (記住一個實例)
    val sharedTable = remember { QSTable() }

    // 之後可換更強的 AI；若 AI 本身無狀態，這樣記住一個實例即可
//    val aiPlayer = remember { FirstEmptyWithRecordAIPlayer() }
    //val aiPlayer = remember { QSTableAIPlayer() }
    // MOD: aiPlayer 注入 sharedTable
    val aiPlayer = remember { QSTableAIPlayer(myType = 2, table = sharedTable) }

// NEW: coroutine scope (非阻塞 self-play)
    val scope = rememberCoroutineScope()

    fun newGame() {
        // 新增：隨機決定誰先手 (1=O 人先, 2=X AI 先)
        val initialState = TicTacToeEngine.createInitialState(randomFirst = true)
        state = initialState

        aiPlayer.resetForGame()
        MyLog.add("New game - AI reset for game, first turn: ${TicTacToeRules.cellToChar(initialState.turn)}")

        // 如果 AI 先手 (turn==2)，立即讓 AI 下第一步
        if (initialState.turn == 2) {
            val aiFirstUpdate = TicTacToeEngine.aiFirstMove(initialState, aiPlayer)
            state = aiFirstUpdate.state
            aiFirstUpdate.logs.forEach { MyLog.add(it) }
            MyLog.add("AI first move executed")
        }
    }

    fun onCellClick(pos: Int) {
        val update = TicTacToeEngine.onCellClick(state, pos, aiPlayer)
        update.logs.forEach { MyLog.add(it) }
        state = update.state
        if (state.gameOver) {
            aiPlayer.refine(state.iGameResult)
            aiPlayer.showRecords()
            gameCount++  // 遊戲結束學習後計數 +1
            MyLog.add("Game learned: total count = $gameCount")
        }
    }

    // 更新：Forget 函數，使用新方法清 QSTable
    fun onForget() {
        aiPlayer.resetForForget()
        gameCount = 0
        MyLog.add("Forgot all records")
    }

    // Analyze 函數不變
    fun onAnalyze() {
        aiPlayer.showRecords()
        MyLog.add("Analyzed records")
    }

    // NEW: GoHome 啟動 sandbox
    fun onGoHome() {
        var times : Int = 1000
        scope.launch {
            val stats = SelfPlaySandbox.runSelfPlay(times, selfPlayRatio = 0.7, sharedTable = sharedTable)
            { completed, stats ->
                if (completed == times) {
                    gameCount += times  // 結束後更新計數 (視為額外學習)
                    MyLog.add("Self-play finished: $times games learned, total Times: $gameCount")
                    MyLog.add("Progress: $completed/$times, SelfPlay WinRate: ${stats.selfPlayWinRate}, VsRandom: ${stats.vsRandomWinRate}")

                } else {
                    MyLog.add("Self-play progress: $completed/$times games")
                }
            }
            MyLog.add("Final: SelfPlay ${stats.selfPlayWinRate}, VsRandom ${stats.vsRandomWinRate}")
        }
        MyLog.add("GoHome clicked: starting 100 self-play games...")
    }

    val viewState = TicTacToePresenter.present(state)

    TicTacToeScreen(
        board = state.board,
        viewState = viewState,
        modifier = modifier,
        onCellClick = ::onCellClick,
        onNewGame = ::newGame,
        onForget = ::onForget,
        onAnalyze = ::onAnalyze,
        onGoHome = ::onGoHome,  // NEW: 傳入
        gameCount = gameCount
    )
}