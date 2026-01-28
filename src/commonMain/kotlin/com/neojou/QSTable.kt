package com.neojou

private fun boardToString(b: BoardStatus): String =
    b.copyArray().joinToString(prefix = "[", postfix = "]", separator = ",")

class QSTable {
    // 修改：key 從 BoardStatus 改為 String (boardToString)，確保相同盤面共享 PTable
    private val qs: HashMap<String, PTable> = HashMap()

    fun clear() {
        qs.clear()
    }

    // 修改：用 string key
    fun getOrCreate(sa: BoardStatus): PTable =
        qs.getOrPut(boardToString(sa)) { PTable() }

    // 修改：用 string key
    fun getOrNull(sa: BoardStatus): PTable? = qs[boardToString(sa)]

    /**
     * If first time encountering this state, build ptable with all legal positions.
     */
    fun ensureBuilt(sa: BoardStatus, legalPos: Iterable<Int>, defaultWeight: Int = 1): PTable {
        val pt = getOrCreate(sa)
        if (!pt.hasBuilt) pt.build(legalPos, defaultWeight)
        return pt
    }

    /**
     * Current stage helper: ensure ptable exists and promote chosen pos.
     * (default=1, chosen=2)
     */
    fun markChosen(sa: BoardStatus, chosenPos: Int, chosenWeight: Int = 2) {
        val pt = getOrCreate(sa)
        pt.setWeightMax(chosenPos, chosenWeight)
    }

    fun show(title: String = "QSTable") {
        MyLog.add("$title size=${qs.size}")
        qs.entries.forEachIndexed { idx, (key, pt) ->
            val ptableString = pt.entries()
                .sortedBy { it.first }
                .joinToString(prefix = "{", postfix = "}") { (pos, w) -> "$pos=$w" }
            MyLog.add("[$idx] sa=$key, hasBuilt=${pt.hasBuilt}, ptable=$ptableString")
        }
    }
}