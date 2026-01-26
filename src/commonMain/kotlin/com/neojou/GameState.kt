package com.neojou

data class GameState(
    val board: BoardStatus = BoardStatus(),
    val turn: Int = 1,       // 1=O, 2=X
    val moves: Int = 0,
    val gameOver: Boolean = false,
    val gameResult: String? = null
)
