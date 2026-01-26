package com.neojou

import kotlin.time.TimeSource

enum class LogLevel { INFO, WARN, ERROR }

data class LogEntry(
    val elapsed: String,
    val level: LogLevel,
    val message: String
)

object MyLog {
    private var consoleOn: Boolean = true
    private val entries: MutableList<LogEntry> = mutableListOf()

    // 程式啟動基準點（單調時間）
    private val startMark = TimeSource.Monotonic.markNow()

    fun turnOnConsole() { consoleOn = true }
    fun turnOffConsole() { consoleOn = false }

    fun add(message: String, level: LogLevel = LogLevel.INFO) {
        val entry = LogEntry(
            elapsed = startMark.elapsedNow().toString(),
            level = level,
            message = message
        )
        entries.add(entry)
        if (consoleOn) println("[${entry.elapsed}] [${entry.level}] ${entry.message}")
    }

    fun getAll(): List<LogEntry> = entries.toList()
    fun clear() { entries.clear() }
}
