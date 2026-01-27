package com.neojou

private fun boardToString(b: BoardStatus): String =
    b.copyArray().joinToString(prefix = "[", postfix = "]", separator = ",")

/**
 * Replay-friendly episode step.
 * - sa: snapshot state
 * - chosenPos: chosen action at sa
 * - legalPos: legal actions at that time (for future RL update / validation)
 * - playerType: optional (1/2), keep nullable for now
 */
data class EpisodeStep(
    val sa: BoardStatus,
    val chosenPos: Int,
    val legalPos: IntArray,
    val playerType: Int? = null,
)

class Episode {
    private val _steps = mutableListOf<EpisodeStep>()
    val steps: List<EpisodeStep> get() = _steps

    fun clear() = _steps.clear()

    fun append(sa: BoardStatus, chosenPos: Int, legalPos: Iterable<Int>, playerType: Int? = null) {
        val arr = legalPos.toList().toIntArray()
        _steps.add(EpisodeStep(sa, chosenPos, arr, playerType))
    }

    /**
     * Future RL hook: iterate from last step backwards.
     */
    fun forEachReversed(action: (EpisodeStep) -> Unit) {
        for (i in _steps.size - 1 downTo 0) action(_steps[i])
    }

    fun show(table: QSTable, title: String = "Episode") {
        MyLog.add("$title size=${_steps.size}")
        _steps.forEachIndexed { idx, step ->
            val pt = table.getOrNull(step.sa)
            val w = pt?.getWeight(step.chosenPos)
            MyLog.add(
                "[$idx] sa=${boardToString(step.sa)}, pos=${step.chosenPos}, weight=$w, legal=${step.legalPos.contentToString()}, player=${step.playerType}"
            )
        }
    }
}
