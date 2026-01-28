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
     * 新增：順時針旋轉 90° 的新 Episode（clone 並轉換每個 step）
     * 利用棋盤對稱性，增加學習資料
     */
    fun clockwise(): Episode {
        val newEpisode = Episode()
        _steps.forEach { step ->
            val rotatedSa = rotateBoard(step.sa)
            val rotatedChosenPos = rotatePos(step.chosenPos)
            val rotatedLegal = step.legalPos.map { rotatePos(it) }.toIntArray().sortedArray()  // 轉換並排序
            newEpisode.append(rotatedSa, rotatedChosenPos, rotatedLegal.toList(), step.playerType)
        }
        return newEpisode
    }

    // 輔助：順時針 90° 旋轉 BoardStatus (3x3 位置映射)
    private fun rotateBoard(sa: BoardStatus): BoardStatus {
        val oldArray = sa.copyArray()
        val newArray = IntArray(9)
        newArray[0] = oldArray[6]  // 0 <- 6
        newArray[1] = oldArray[3]  // 1 <- 3
        newArray[2] = oldArray[0]  // 2 <- 0
        newArray[3] = oldArray[7]  // 3 <- 7
        newArray[4] = oldArray[4]  // 4 <- 4 (中心不變)
        newArray[5] = oldArray[1]  // 5 <- 1
        newArray[6] = oldArray[8]  // 6 <- 8
        newArray[7] = oldArray[5]  // 7 <- 5
        newArray[8] = oldArray[2]  // 8 <- 2
        return BoardStatus(newArray)
    }

    // 輔助：順時針 90° 旋轉單一位置 (0-8 映射)
    private fun rotatePos(oldPos: Int): Int {
        return when (oldPos) {
            0 -> 2
            1 -> 5
            2 -> 8
            3 -> 1
            4 -> 4
            5 -> 7
            6 -> 0
            7 -> 3
            8 -> 6
            else -> oldPos  // 無效 pos 保持
        }
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
        winDelta: Int = 5,
        loseDelta: Int = 1,
        drawDelta: Int = 2,
        gamma: Double = 0.95, // 先保留；目前可設 1.0 等於不折扣
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