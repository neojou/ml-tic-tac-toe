package com.neojou

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        DisposableEffect(Unit) {
            MyMacOSIntegration.installAboutHandler { aboutOpen = true }
            onDispose { MyMacOSIntegration.clearAboutHandler() }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = AppBuildInfo.appName
        ) {
            App(
                buildInfo = buildInfo,
                aboutOpen = aboutOpen,
                onOpenAbout = { aboutOpen = true },
                onCloseAbout = { aboutOpen = false },
            )
        }
    }
}
