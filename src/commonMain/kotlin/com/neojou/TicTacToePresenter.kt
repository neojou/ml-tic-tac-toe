package com.neojou

object TicTacToePresenter {
    fun present(state: GameState): TicTacToeViewState {
        if (state.gameOver && state.gameResult != null) {
            return TicTacToeViewState(title = state.gameResult, isResult = true)
        }
        if (state.moves == 0) {
            return TicTacToeViewState(title = "Game Start", subtitle = "O’s turn")
        }
        val round = state.moves + 1
        val turnChar = TicTacToeRules.cellToChar(state.turn)
        return TicTacToeViewState(title = "Turn Round : $round", subtitle = "$turnChar’s turn")
    }
}

