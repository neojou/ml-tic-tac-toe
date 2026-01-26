package com.neojou

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun App(
    buildInfo: BuildInfo,
    aboutOpen: Boolean,
    onOpenAbout: () -> Unit,
    onCloseAbout: () -> Unit,
) {
    MaterialTheme {
        TicTacToeGame(modifier = Modifier.fillMaxSize())

        if (aboutOpen) {
            AboutDialog(buildInfo = buildInfo, onClose = onCloseAbout)
        }
    }
}
