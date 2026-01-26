package com.neojou

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextOverflow
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
    val topBarHeight = 40.dp
    val bottomBarHeight = 120.dp

    Column(modifier = modifier.fillMaxSize()) {
        TopMenuBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight),
            onNewGame = onNewGame
        )

        // 中間區塊：吃剩餘高度，但至少留一個合理高度，避免初始 0
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            // 只依中間區塊 constraints 決定棋盤大小，這樣 bottom 一定不會被棋盤吃掉
            val side = minOf(maxWidth, maxHeight) * 0.92f

            // 假如 maxHeight 初始仍偶發 0，就給個保底，至少會顯示棋盤
            val safeSide = if (side <= 0.dp) 180.dp else side

            TicTacToeBoard(
                bs = board,
                modifier = Modifier.size(safeSide),
                onCellClick = { pos ->
                    if (clickEnabled) onCellClick(pos)
                }
            )
        }

        // 如果中間區被縮得很小，Spacer 會吃掉多餘空間，bottom 仍固定在底部
        Spacer(modifier = Modifier.height(0.dp))

        BottomBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomBarHeight)
                .padding(vertical = 6.dp),
            viewState = viewState
        )
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                style = if (viewState.isResult) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
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
