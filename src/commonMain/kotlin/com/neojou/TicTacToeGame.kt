package com.neojou

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun TicTacToeGame(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(GameState()) }

    fun onCellClick(pos: Int) {
        val update = TicTacToeEngine.onCellClick(state, pos)
        update.logs.forEach { MyLog.add(it) }
        state = update.state
    }

    // UI 相關推導：放在事件函式外面，讓它跟著 state 重組更新
    val viewState = TicTacToePresenter.present(state)

    TicTacToeScreen(
        board = state.board,
        viewState = viewState,
        modifier = modifier,
        clickEnabled = !state.gameOver,
        onCellClick = ::onCellClick
    )
}
