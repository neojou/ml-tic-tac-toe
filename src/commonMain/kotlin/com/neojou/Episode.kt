package com.neojou

private fun boardToString(b: BoardStatus): String =
    b.copyArray().joinToString(prefix = "[", postfix = "]", separator = ",")

/**
 * Replay-friendly episode step.
 * - sa: snapshot state
 * - chosenPos: chosen action at sa
 * - legalPos: legal actions at that time (for future RL update / validation)
 * - playerType: 1(O) or 2(X)
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

    fun forEachReversed(action: (EpisodeStep) -> Unit) {
        for (i in _steps.size - 1 downTo 0) action(_steps[i])
    }

    /**
     * Game end refinement (very simple version, per your current idea):
     * - O wins: O moves +10, X moves +1
     * - X wins: X moves +10, O moves +1
     * - Draw: all moves +5
     *
     * You can later replace this with proper RL (reward, gamma discount, backward update, etc.)
     */
    fun refine(
        table: QSTable,
        outcome: Int,
        winDelta: Int = 10,
        loseDelta: Int = 1,
        drawDelta: Int = 5,
        gamma: Double = 1.0, // 先保留；目前可設 1.0 等於不折扣
    ) {
        /*
        // parse outcome from your current GameState.gameResult text
        val outcome: Int? = when {
            gameResult.startsWith("O") -> 1
            gameResult.startsWith("X") -> 2
            gameResult.startsWith("Draw") -> 0
            else -> null
        } */

        var factor = 1.0 // terminal step factor=1, previous *= gamma ...

        forEachReversed { step ->
            val pt = table.ensureBuilt(step.sa, step.legalPos.toList(), defaultWeight = 1)

            val baseDelta = when (outcome) {
                0 -> drawDelta
                1 -> if (step.playerType == 1) winDelta else loseDelta
                2 -> if (step.playerType == 2) winDelta else loseDelta
                else -> 0
            }

            // 若你之後要折扣：讓越接近終局的步驟加分越大（或越小），可調 gamma
            val delta = (baseDelta * factor).toInt()

            if (delta != 0) {
                pt.addWeight(step.chosenPos, delta)
            }

            factor *= gamma
        }
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
