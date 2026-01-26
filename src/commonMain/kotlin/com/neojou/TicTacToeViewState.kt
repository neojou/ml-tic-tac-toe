package com.neojou

data class TicTacToeViewState(
    val title: String,
    val subtitle: String? = null,
    val hint: String = "Use Mouse to Click",
    val isResult: Boolean = false
)
