package com.neojou

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AboutDialog(buildInfo: BuildInfo, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("About") },
        text = {
            Text("${buildInfo.appName} ${buildInfo.version}\nBuild on ${buildInfo.buildTime}")
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("OK") }
        }
    )
}
