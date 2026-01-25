package com.neojou

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.draw.clipToBounds


@Composable
fun TicTacToeGame(
    modifier: Modifier = Modifier
) {
    // 0=empty, 1=O, 2=X
    var board by remember { mutableStateOf(IntArray(9) { 0 }) }
    var turn by remember { mutableStateOf(1) }    // 第一回合固定 O(1)
    var moves by remember { mutableStateOf(0) }   // 有效落子次數
    var gameOver by remember { mutableStateOf(false) }
    var gameResult by remember { mutableStateOf<String?>(null) } // "O is the winner" / "X is the winner" / "Draw"

    fun cellToChar(v: Int): String = when (v) {
        1 -> "O"
        2 -> "X"
        else -> " "
    }

    fun checkWinner(b: IntArray): Int {
        val lines = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6),
        )
        for (ln in lines) {
            val a = b[ln[0]]
            if (a != 0 && a == b[ln[1]] && a == b[ln[2]]) return a
        }
        return 0
    }

    fun onClick(pos: Int) {
        if (gameOver) return

        val cur = board[pos]
        if (cur != 0) {
            MyLog.add("Mouse Click on A[$pos], A[$pos] has been put ${cellToChar(cur)} already")
            return
        }

        // 有效 click：落子（產生新陣列，確保 UI 更新）
        val nextBoard = board.copyOf()
        nextBoard[pos] = turn
        board = nextBoard
        moves += 1

        MyLog.add("Mouse Click on A[$pos] , A[$pos] is ${cellToChar(turn)}")

        // 判定勝負/和局
        val winner = checkWinner(nextBoard)
        when (winner) {
            1 -> {
                MyLog.add("O is the winner")
                gameOver = true
                gameResult = "O is the winner"
                return
            }
            2 -> {
                MyLog.add("X is the winner")
                gameOver = true
                gameResult = "X is the winner"
                return
            }
        }

        if (moves >= 9) {
            MyLog.add("Draw")
            gameOver = true
            gameResult = "Draw"
            return
        }

        // 換手
        turn = if (turn == 1) 2 else 1
    }

    val bs = remember(board) { BoardStatus(board) }

    Column(modifier = modifier.fillMaxSize()) {
        // 上方：棋盤（4/5）
        Box(
            modifier = Modifier.weight(4f).fillMaxWidth().clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints {
                val side = if (maxWidth < maxHeight) maxWidth else maxHeight

                TicTacToeBoard(
                    bs = bs,
                    modifier = Modifier.size(side * 0.95f), // 0.95f 可自行調整邊距
                    onCellClick = { pos -> onClick(pos) }
                )
            }
        }

        // 下方：狀態列（1/5）
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    gameOver && gameResult != null -> {
                        Text(gameResult!!, style = MaterialTheme.typography.titleLarge)
                    }
                    moves == 0 -> {
                        Text("Game Start", style = MaterialTheme.typography.titleMedium)
                        Text("O’s turn", style = MaterialTheme.typography.titleMedium)
                        Text("Use Mouse to Click", style = MaterialTheme.typography.bodyMedium)
                    }
                    else -> {
                        val round = moves + 1
                        Text("Turn Round : $round", style = MaterialTheme.typography.titleMedium)
                        Text("${cellToChar(turn)}’s turn", style = MaterialTheme.typography.titleMedium)
                        Text("Use Mouse to Click", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
