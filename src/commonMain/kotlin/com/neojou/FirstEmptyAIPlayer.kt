package com.neojou

open class FirstEmptyAIPlayer : AIPlayer {

    override fun chooseMove(board: BoardStatus): Int? {
        return BoardAnalyze.getEmptyPosSet(board).minOrNull()
    }

    override fun showRecords() {
        return
    }

    override fun addLastMove(board: BoardStatus) {
        return
    }

}
