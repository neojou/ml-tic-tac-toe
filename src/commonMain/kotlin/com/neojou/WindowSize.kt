package com.neojou

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntSize

@Composable
expect fun rememberWindowSize(): IntSize?

