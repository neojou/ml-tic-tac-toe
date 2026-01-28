package com.neojou

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TicTacToeScreen(
    board: BoardStatus,
    viewState: TicTacToeViewState,
    modifier: Modifier = Modifier,
    clickEnabled: Boolean = true,
    onCellClick: (Int) -> Unit,
    onNewGame: () -> Unit,
    onForget: () -> Unit = {},  // 新增：Forget 按鈕的回調
    onAnalyze: () -> Unit = {}, // 新增：Analyze 按鈕的回調
    onGoHome: () -> Unit = {},  // NEW
    gameCount: Int = 0,        // 新增：AI 學習場數，用於顯示
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopMenuBar(
                modifier = Modifier.fillMaxWidth(),
                onNewGame = onNewGame,
                onForget = onForget,    // 傳入
                onAnalyze = onAnalyze,  // 傳入
                onGoHome = onGoHome,  // NEW: 傳入
                gameCount = gameCount   // 新增：傳入計數
            )
        },
        bottomBar = {
            BottomBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                viewState = viewState
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            // 只依 content 的 constraints 決定棋盤大小（避免吃到 top/bottom 的空間）
            val side = minOf(maxWidth, maxHeight) * 0.92f
            // 保底：避免偶發 constraints 為 0 時棋盤不顯示
            val safeSide = if (side <= 0.dp) 180.dp else side

            TicTacToeBoard(
                bs = board,
                modifier = Modifier.size(safeSide),
                onCellClick = { pos ->
                    if (clickEnabled) onCellClick(pos)
                }
            )
        }
    }
}

@Composable
private fun TopMenuBar(
    modifier: Modifier = Modifier,
    onNewGame: () -> Unit,
    onForget: () -> Unit,    // 新增參數
    onAnalyze: () -> Unit,   // 新增參數
    onGoHome: () -> Unit,  // NEW
    gameCount: Int = 0,      // 新增：AI 學習場數
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly  // 新增：平均分佈按鈕
    ) {
        TextButton(
            onClick = onNewGame,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "New",
                style = MaterialTheme.typography.titleSmall
            )
        }

        // 新增：Forget 按鈕
        TextButton(
            onClick = onForget,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Forget",
                style = MaterialTheme.typography.titleSmall
            )
        }

        // 新增：Analyze 按鈕
        TextButton(
            onClick = onAnalyze,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Analyze",
                style = MaterialTheme.typography.titleSmall
            )
        }

// NEW: GoHome 按鈕
        TextButton(
            onClick = onGoHome,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "GoHome",
                style = MaterialTheme.typography.titleSmall
            )
        }

        // 新增：在 Analyze 旁邊顯示 Times: NN
        Text(
            text = "Times: $gameCount",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 4.dp)  // 小間距
        )
    }
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    viewState: TicTacToeViewState,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = viewState.title,
                style = if (viewState.isResult)
                    MaterialTheme.typography.titleLarge
                else
                    MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = viewState.subtitle ?: " ",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = viewState.hint,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}