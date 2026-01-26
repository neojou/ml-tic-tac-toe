package com.neojou

class FirstEmptyAIPlayer : AIPlayer {
    override fun chooseMove(board: BoardStatus): Int? {
        for (i in 0..8) {
            if (board[i] == 0) return i
        }
        return null
    }
}

