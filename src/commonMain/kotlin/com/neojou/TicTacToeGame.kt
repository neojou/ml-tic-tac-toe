package com.neojou

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun TicTacToeGame(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(GameState()) }

    // 之後可換更強的 AI；若 AI 本身無狀態，這樣記住一個實例即可
//    val aiPlayer = remember { FirstEmptyWithRecordAIPlayer() }
    val aiPlayer = remember { QSTableAIPlayer() }

    fun newGame() {
        state = GameState()
        MyLog.add("New game")
    }

    fun onCellClick(pos: Int) {
        val update = TicTacToeEngine.onCellClick(state, pos, aiPlayer)
        update.logs.forEach { MyLog.add(it) }
        state = update.state
        if (state.gameOver) {
            aiPlayer.refine(state.iGameResult)
            aiPlayer.showRecords()
        }
    }

    // 新增：Forget 函數（假設 AI 有 clearRecords 方法來清空學習記錄）
    fun onForget() {
        aiPlayer.clearRecords()  // 請根據 QSTableAIPlayer 的實際 API 調整，例如清空 Q-Table
        MyLog.add("Forgot all records")
    }

    // 新增：Analyze 函數（使用現有的 showRecords 來顯示分析）
    fun onAnalyze() {
        aiPlayer.showRecords()
        MyLog.add("Analyzed records")
    }

    val viewState = TicTacToePresenter.present(state)

    TicTacToeScreen(
        board = state.board,
        viewState = viewState,
        modifier = modifier,
        onCellClick = ::onCellClick,
        onNewGame = ::newGame,
        onForget = ::onForget,   // 傳入新函數
        onAnalyze = ::onAnalyze  // 傳入新函數
    )
}