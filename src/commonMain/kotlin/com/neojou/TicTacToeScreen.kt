package com.neojou

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp

data class TicTacToeViewState(
    val title: String,
    val subtitle: String? = null,
    val hint: String = "Use Mouse to Click",
    val isResult: Boolean = false
)

@Composable
fun TicTacToeScreen(
    board: BoardStatus,
    viewState: TicTacToeViewState,
    modifier: Modifier = Modifier,
    clickEnabled: Boolean = true,
    onCellClick: (Int) -> Unit,
    onNewGame: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {

        // Top "menu bar"：像傳統選單列（左上角 Game ▾，點開有 New）
        TopMenuBar(
            modifier = Modifier.fillMaxWidth(),
            onNewGame = onNewGame
        )

        // 7/10：棋盤
        Box(
            modifier = Modifier
                .weight(7f)
                .fillMaxWidth()
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints {
                val side = if (maxWidth < maxHeight) maxWidth else maxHeight
                TicTacToeBoard(
                    bs = board,
                    modifier = Modifier.size(side * 0.95f),
                    onCellClick = { pos ->
                        if (clickEnabled) onCellClick(pos)
                    }
                )
            }
        }

        // 1/5 (=2/10)：訊息
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewState.isResult) {
                    Text(viewState.title, style = MaterialTheme.typography.titleLarge)
                } else {
                    Text(viewState.title, style = MaterialTheme.typography.titleMedium)
                }
                viewState.subtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                Text(viewState.hint, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TopMenuBar(
    modifier: Modifier = Modifier,
    onNewGame: () -> Unit,
) {
    var gameMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Anchor：把「Game ▾」包在 Box，DropdownMenu 也放同一個 Box 內（最穩）
        Box {
            Text(
                text = "Game ▾",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clickable { gameMenuOpen = true }
            )

            DropdownMenu(
                expanded = gameMenuOpen,
                onDismissRequest = { gameMenuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("New") },
                    onClick = {
                        gameMenuOpen = false
                        onNewGame()
                    }
                )
            }
        }
    }
}
