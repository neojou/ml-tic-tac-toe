package com.neojou

data class RecordRow(
    val round: Int,
    val sa: BoardStatus, // snapshot
    val pos: Int,
    var score: Int
)

private fun boardToString(b: BoardStatus): String =
    b.copyArray().joinToString(prefix = "[", postfix = "]", separator = ",")

private fun rowToString(r: RecordRow): String =
    "round=${r.round}, sa=${boardToString(r.sa)}, pos=${r.pos}, score=${r.score}"

class RecordTable {
    private val _rows = mutableListOf<RecordRow>()
    val rows: List<RecordRow> get() = _rows

    fun add(round: Int, sa: BoardStatus, pos: Int, score: Int): RecordRow {
        val row = RecordRow(round, sa, pos, score)
        _rows.add(row)
        return row
    }

    fun clear() = _rows.clear()

    fun show(title: String = "RecordTable") {
        MyLog.add("$title size=${_rows.size}")
        _rows.forEachIndexed { idx, r ->
            MyLog.add("[$idx] ${rowToString(r)}")
        }
    }
}

class Episode {
    private val _steps = mutableListOf<RecordRow>()
    val steps: List<RecordRow> get() = _steps

    fun append(row: RecordRow) {
        _steps.add(row) // 同一個 RecordRow 物件引用 [file:5]
    }

    fun clear() = _steps.clear()

    fun show(title: String = "Episode") {
        MyLog.add("$title size=${_steps.size}")
        _steps.forEachIndexed { idx, r ->
            MyLog.add("[$idx] ${rowToString(r)}")
        }
    }
}
