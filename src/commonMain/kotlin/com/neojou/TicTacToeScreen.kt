package com.neojou

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onNewGame: () -> Unit, // 新增：上方 menu bar 的 New
) {
    Column(modifier = modifier.fillMaxSize()) {

        // 1/10：頂部 menu bar
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "New",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clickable { onNewGame() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }

        // 7/10：井字遊戲畫面（棋盤）
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

        // 1/5：訊息視窗（= 2/10）
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
