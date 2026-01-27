package com.neojou

private fun boardToString(b: BoardStatus): String =
    b.copyArray().joinToString(prefix = "[", postfix = "]", separator = ",")

class QSTable {
    private val qs: HashMap<BoardStatus, PTable> = HashMap()

    fun clear() = qs.clear()

    fun getOrCreate(sa: BoardStatus): PTable =
        qs.getOrPut(sa) { PTable() }

    fun getOrNull(sa: BoardStatus): PTable? = qs[sa]

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
        // pt might not be built yet (caller should usually ensureBuilt first),
        // but setWeightMax is still safe.
        pt.setWeightMax(chosenPos, chosenWeight)
    }

    fun show(title: String = "QSTable") {
        MyLog.add("$title size=${qs.size}")
        qs.entries.forEachIndexed { idx, (sa, pt) ->
            val ptableString = pt.entries()
                .sortedBy { it.first }
                .joinToString(prefix = "{", postfix = "}") { (pos, w) -> "$pos=$w" }
            MyLog.add("[$idx] sa=${boardToString(sa)}, hasBuilt=${pt.hasBuilt}, ptable=$ptableString")
        }
    }
}
