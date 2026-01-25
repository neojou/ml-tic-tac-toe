package com.neojou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App(
    buildInfo: BuildInfo,
    aboutOpen: Boolean,
    onOpenAbout: () -> Unit,
    onCloseAbout: () -> Unit,
) {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hello World")
            Text("2026")
        }

        if (aboutOpen) {
            AboutDialog(buildInfo = buildInfo, onClose = onCloseAbout)
        }
    }
}
