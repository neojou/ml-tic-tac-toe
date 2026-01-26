package com.neojou

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

private fun setupMacAppName() {
    System.setProperty("apple.awt.application.name", AppBuildInfo.appName)
}

fun main() {
    MySystemInfo.showAll()
    setupMacAppName()

    application {
        val buildInfo = remember { AppBuildInfo.asBuildInfo() }
        var aboutOpen by remember { mutableStateOf(false) }

        // 用這個 key 來重開一局
        var gameKey by remember { mutableStateOf(0) }

        DisposableEffect(Unit) {
            MyMacOSIntegration.installAboutHandler { aboutOpen = true }
            onDispose { MyMacOSIntegration.clearAboutHandler() }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = AppBuildInfo.appName
        ) {
            MenuBar {
                Menu("Game") {
                    Item(
                        "New",
                        onClick = {
                            gameKey += 1
                            // 可選：清 console log 記錄
                            // MyLog.clear()
                            MyLog.add("New game")
                        }
                    )
                }
            }

            App(
                buildInfo = buildInfo,
                aboutOpen = aboutOpen,
                onOpenAbout = { aboutOpen = true },
                onCloseAbout = { aboutOpen = false },
                gameKey = gameKey
            )
        }
    }
}
