package com.neojou

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App(
    buildInfo: BuildInfo,
    aboutOpen: Boolean,
    onOpenAbout: () -> Unit,
    onCloseAbout: () -> Unit,
) {
    MaterialTheme {
        // 固定棋盤測試資料（1=O, 2=X）
        val bs = remember {
            BoardStatus(intArrayOf(1, 2, 0, 2, 1, 0, 0, 2, 1))
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TicTacToeBoard(
                bs = bs,
                modifier = Modifier.fillMaxWidth(0.75f), // 棋盤大小可自行調整
                onCellClick = { pos ->
                    // 測試：先印出你點到哪一格（0..8）
                    println("clicked pos=$pos")
                }
            )
        }

        if (aboutOpen) {
            AboutDialog(buildInfo = buildInfo, onClose = onCloseAbout)
        }
    }
}
