package com.neojou

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
actual fun rememberWindowSize(): IntSize? {
    fun readNow(): IntSize =
        IntSize(
            window.innerWidth.coerceAtLeast(1),
            window.innerHeight.coerceAtLeast(1)
        )

    var size by remember { mutableStateOf(readNow()) }

    DisposableEffect(Unit) {
        fun update() {
            size = readNow()
        }

        val prev = window.onresize
        window.onresize = { e ->
            update()
            prev?.invoke(e)
        }

        // 初次載入補幾個 event-loop tick（避免「第一次不出現，resize 才出現」）
        // 只用 delay(0)，不依賴實際時間
        val scope = MainScope()
        scope.launch {
            repeat(12) {
                delay(0)
                update()
            }
        }

        onDispose {
            window.onresize = prev
        }
    }

    return size
}
