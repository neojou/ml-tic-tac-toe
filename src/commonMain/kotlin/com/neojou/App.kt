package com.neojou

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TicTacToeBoard(
                modifier = Modifier.fillMaxWidth(0.75f) // 棋盤大小可自行調整
            )
        }

        if (aboutOpen) {
            AboutDialog(buildInfo = buildInfo, onClose = onCloseAbout)
        }
    }
}
