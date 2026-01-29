package com.neojou

import kotlin.time.TimeSource

enum class LogLevel(val priority: Int) {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3)
}

data class LogEntry(
    val elapsed: String,
    val level: LogLevel,
    val module: String,
    val message: String
)

object MyLog {
    private var consoleOn: Boolean = true
    private val entries: MutableList<LogEntry> = mutableListOf()

    // 全域最低輸出等級（預設 INFO）
    private var globalMinLevel: LogLevel = LogLevel.INFO

    // module 覆寫：例如 "AI" -> DEBUG
    private val moduleMinLevels: MutableMap<String, LogLevel> = mutableMapOf()

    // 程式啟動基準點（單調時間）
    private val startMark = TimeSource.Monotonic.markNow()

    fun turnOnConsole() { consoleOn = true }
    fun turnOffConsole() { consoleOn = false }

    fun setGlobalMinLevel(level: LogLevel) { globalMinLevel = level }

    fun setModuleMinLevel(module: String, level: LogLevel) {
        moduleMinLevels[module] = level
    }

    fun clearModuleMinLevel(module: String) {
        moduleMinLevels.remove(module)
    }

    private fun shouldPrint(level: LogLevel, module: String): Boolean {
        if (!consoleOn) return false
        val minLevel = moduleMinLevels[module] ?: globalMinLevel
        return level.priority >= minLevel.priority
    }

    fun add(module: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = LogEntry(
            elapsed = startMark.elapsedNow().toString(),
            level = level,
            module = module,
            message = message
        )
        entries.add(entry)

        if (shouldPrint(level, module)) {
            println("[${entry.elapsed}] [${entry.level}] [${entry.module}] ${entry.message}")
        }
    }

    fun getAll(): List<LogEntry> = entries.toList()

    fun getFiltered(module: String? = null, minLevel: LogLevel = LogLevel.DEBUG): List<LogEntry> {
        return entries.filter {
            (module == null || it.module == module) && it.level.priority >= minLevel.priority
        }
    }

    fun clear() { entries.clear() }
}
