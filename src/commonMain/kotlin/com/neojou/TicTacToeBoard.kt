package com.neojou

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun TicTacToeBoard(
    bs: BoardStatus,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Black,
    lineWidth: Dp = 4.dp,
    onCellClick: (Int) -> Unit = {}
) {
    var boardSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier
            .onSizeChanged { boardSize = it }
            // 重要：用 boardSize 當 key，resize 時 pointerInput 會重啟，命中區才會更新
            .pointerInput(boardSize) {
                detectTapGestures { p ->
                    val cellW = boardSize.width / 3f
                    val cellH = boardSize.height / 3f
                    if (cellW <= 0f || cellH <= 0f) return@detectTapGestures

                    val c = (p.x / cellW).toInt().coerceIn(0, 2)
                    val r = (p.y / cellH).toInt().coerceIn(0, 2)
                    onCellClick(r * 3 + c)
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val stroke = lineWidth.toPx()

        val x1 = w / 3f
        val x2 = w * 2f / 3f
        val y1 = h / 3f
        val y2 = h * 2f / 3f

        drawLine(lineColor, Offset(x1, 0f), Offset(x1, h), strokeWidth = stroke)
        drawLine(lineColor, Offset(x2, 0f), Offset(x2, h), strokeWidth = stroke)
        drawLine(lineColor, Offset(0f, y1), Offset(w, y1), strokeWidth = stroke)
        drawLine(lineColor, Offset(0f, y2), Offset(w, y2), strokeWidth = stroke)

        val cellW = w / 3f
        val cellH = h / 3f
        val cellMin = min(cellW, cellH)
        val pad = cellMin * 0.18f

        for (pos in 0..8) {
            when (bs[pos]) {
                0 -> Unit
                1 -> {
                    val r = pos / 3
                    val c = pos % 3
                    val cx = c * cellW + cellW / 2f
                    val cy = r * cellH + cellH / 2f
                    drawCircle(
                        color = lineColor,
                        radius = (cellMin / 2f) - pad,
                        center = Offset(cx, cy),
                        style = Stroke(width = stroke)
                    )
                }
                2 -> {
                    val r = pos / 3
                    val c = pos % 3
                    val left = c * cellW
                    val top = r * cellH

                    val xL = left + pad
                    val xR = left + cellW - pad
                    val yT = top + pad
                    val yB = top + cellH - pad

                    drawLine(lineColor, Offset(xL, yT), Offset(xR, yB), strokeWidth = stroke)
                    drawLine(lineColor, Offset(xR, yT), Offset(xL, yB), strokeWidth = stroke)
                }
                else -> Unit
            }
        }
    }
}
