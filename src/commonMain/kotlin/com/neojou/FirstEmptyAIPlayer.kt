package com.neojou

open class FirstEmptyAIPlayer : AIPlayer {

    override fun chooseMove(board: BoardStatus): Int? {
        return BoardAnalyze.getEmptyPosSet(board).minOrNull()
    }

}
