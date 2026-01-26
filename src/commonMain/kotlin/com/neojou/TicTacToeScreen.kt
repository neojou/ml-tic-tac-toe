package com.neojou

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds

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
    onCellClick: (Int) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {

        Box(
            modifier = Modifier.weight(4f).fillMaxWidth().clipToBounds(),
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

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
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
                    viewState.subtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                    Text(viewState.hint, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

