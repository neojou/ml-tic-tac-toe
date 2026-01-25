package com.neojou

enum class LogLevel { INFO, WARN, ERROR }

data class LogEntry(
    val timestampIso: String,
    val level: LogLevel,
    val message: String
)

object MyLog {
    private var consoleOn: Boolean = true
    private val entries: MutableList<LogEntry> = mutableListOf()

    fun turnOnConsole() { consoleOn = true }
    fun turnOffConsole() { consoleOn = false }

    fun add(message: String, level: LogLevel = LogLevel.INFO) {
        val entry = LogEntry(
            timestampIso = kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().toString(), // 先簡化
            level = level,
            message = message
        )
        entries.add(entry)
        if (consoleOn) println("[${entry.timestampIso}] [${entry.level}] ${entry.message}")
    }

    fun getAll(): List<LogEntry> = entries.toList()
    fun clear() { entries.clear() }
}
