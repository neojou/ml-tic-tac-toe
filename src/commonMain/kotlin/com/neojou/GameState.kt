package com.neojou

data class GameState(
    val board: BoardStatus = BoardStatus(),
    val turn: Int = 1,       // 1=O, 2=X
    val moves: Int = 0,
    val gameOver: Boolean = false,
    val iGameResult: Int = 0, // Draw : 0, O: 1, X: 2
    val gameResult: String? = null
)
