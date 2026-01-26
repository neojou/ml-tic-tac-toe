package com.neojou

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App(
            buildInfo = BuildInfo(
                appName = "ml-tic-tac-toe",
                version = "dev",
                buildTime = "now"
            ),
            aboutOpen = false,
            onOpenAbout = {},
            onCloseAbout = {}
        )
    }
}
