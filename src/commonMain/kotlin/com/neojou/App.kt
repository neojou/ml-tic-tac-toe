package com.neojou

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun App() {
    MaterialTheme {
        TicTacToeGame(modifier = Modifier.fillMaxSize())
        // NEW: 未來若需 About，在這裡加 TopAppBar { IconButton { AboutDialog() } }
    }
}