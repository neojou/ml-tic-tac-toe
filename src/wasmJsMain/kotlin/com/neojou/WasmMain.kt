package com.neojou

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }

    // NEW: 移除 launch + dispatchEvent（Wasm 限制 js() in coroutine）；用佈局 min size 解決初始 0
    // 如果還需 top-level JS 觸發，未來用 expect/actual 包裝
}