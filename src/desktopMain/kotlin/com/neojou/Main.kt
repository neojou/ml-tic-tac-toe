package com.neojou

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    // NEW: 移除 setupMacAppName、MySystemInfo.showAll() 等
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ML Tic-Tac-Toe"  // 硬碼標題，未來用 AppBuildInfo
        ) {
            App()  // 直接呼叫 common App()
        }
    }
}