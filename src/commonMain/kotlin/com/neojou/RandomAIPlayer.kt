package com.neojou


class RandomAIPlayer : AIPlayer {
    override fun chooseMove(board: BoardStatus): Int? {
        // 只實現必須的
        val emptyPos = BoardAnalyze.getEmptyPosSet(board)
        return if (emptyPos.isEmpty()) null else emptyPos.random()
    }
    // 其他方法不寫，自動用接口的 { return }
}