package com.neojou

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun TicTacToeGame(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(GameState()) }
    val aiPlayer = remember { FirstEmptyAIPlayer() } // 之後可換更強的 AI

    fun onCellClick(pos: Int) {
        val update = TicTacToeEngine.onCellClick(state, pos, aiPlayer)
        update.logs.forEach { MyLog.add(it) }
        state = update.state
    }

    val viewState = TicTacToePresenter.present(state)

    TicTacToeScreen(
        board = state.board,
        viewState = viewState,
        modifier = modifier,
        onCellClick = ::onCellClick
    )
}
