package com.neojou

interface AIPlayer {
    /**
     * @return 要下的位置 0..8；若無可下則回傳 null
     */
    fun chooseMove(board: BoardStatus): Int?
}

