package com.neojou

data class GameUpdate(
    val state: GameState,
    val logs: List<String> = emptyList()
)

object TicTacToeEngine {

    fun onCellClick(prev: GameState, pos: Int): GameUpdate {
        if (prev.gameOver) return GameUpdate(prev)

        val cur = prev.board[pos]
        if (cur != 0) {
            return GameUpdate(
                prev,
                listOf(
                    "Mouse Click on A[$pos], A[$pos] has been put ${TicTacToeRules.cellToChar(cur)} already"
                )
            )
        }

        val nextBoard = BoardStatus(prev.board.copyArray())
        nextBoard.set(pos, prev.turn)

        val moves2 = prev.moves + 1
        val logs = mutableListOf<String>(
            "Mouse Click on A[$pos] , A[$pos] is ${TicTacToeRules.cellToChar(prev.turn)}"
        )

        val winner = TicTacToeRules.checkWinner(nextBoard)
        if (winner == 1) {
            return GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, gameResult = "O is the winner"),
                logs + "O is the winner"
            )
        }
        if (winner == 2) {
            return GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, gameResult = "X is the winner"),
                logs + "X is the winner"
            )
        }

        if (TicTacToeRules.isDraw(moves2, winner)) {
            return GameUpdate(
                prev.copy(board = nextBoard, moves = moves2, gameOver = true, gameResult = "Draw"),
                logs + "Draw"
            )
        }

        val nextTurn = if (prev.turn == 1) 2 else 1
        return GameUpdate(
            prev.copy(board = nextBoard, moves = moves2, turn = nextTurn),
            logs
        )
    }
}
