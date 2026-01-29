package com.neojou

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun App() {
    // remember的 第一次組合時會執行，並在後續重組時重用，以避免每次重組都重跑初始化
    remember {
        SystemSettings.initOnce()
        true
    }

    MaterialTheme {
        TicTacToeGame(modifier = Modifier.fillMaxSize())
    }
}
