import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class LogLevel { INFO, WARN, ERROR } // Kotlin enum class 用法 [web:28]

data class LogEntry(
    val timestamp: Instant,
    val level: LogLevel,
    val message: String
)

object MyLog {
    private var consoleOn: Boolean = true
    private val entries: MutableList<LogEntry> = mutableListOf() // mutableListOf 建立可變 list [web:10]

    // 想要顯示在 console 的時間格式（可自行調整）
    private val tsFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())

    fun turnOnConsole() { consoleOn = true }

    fun turnOffConsole() { consoleOn = false }

    fun add(message: String, level: LogLevel = LogLevel.INFO, timestamp: Instant = Instant.now()) {
        val entry = LogEntry(timestamp = timestamp, level = level, message = message)
        entries.add(entry)

        if (consoleOn) {
            println(format(entry))
        }
    }

    fun getAll(): List<LogEntry> = entries.toList()

    fun clear() { entries.clear() }

    private fun format(e: LogEntry): String {
        val ts = tsFormatter.format(e.timestamp)
        return "[$ts] [${e.level}] ${e.message}"
    }
}
