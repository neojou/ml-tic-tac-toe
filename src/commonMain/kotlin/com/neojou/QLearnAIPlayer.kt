package com.neojou

import kotlin.math.max
import kotlin.random.Random

class QLearnAIPlayer(
    val myType: Int = 2,
    val brain: QLearnBrain,
    private val alpha: Double = 0.1,
    private val gamma: Double = 0.95,
    private var epsilon: Double = 0.1,
    private val random: Random = Random.Default
) : AIPlayer {

    private val episodeTransitions = mutableListOf<Step>()

    private fun getQValues(key: String): DoubleArray = brain.qValuesOf(key)

    override fun chooseMove(board: BoardStatus): Int? {
        val legal = BoardAnalyze.getEmptyPosSet(board)
        if (legal.isEmpty()) return null

        val key = brain.keyOf(board)
        val qValues = getQValues(key)

        val isExploration = random.nextDouble() < epsilon
        val action = if (isExploration) {
            legal.random(random)
        } else {
            var maxQ = -Double.MAX_VALUE
            var best = legal.first()
            for (pos in legal) {
                val q = qValues[pos]
                if (q > maxQ) {
                    maxQ = q
                    best = pos
                }
            }
            best
        }

        // 記錄當前 s, a（r 和 s' 由 recordStepOutcome 補上）
        episodeTransitions.add(
            Step(s = snap(board), a = action, r = 0.0, sNext = snap(board)) // sNext 先占位
        )
        return action
    }

    fun recordStepOutcome(reward: Double, nextBoard: BoardStatus) {
        val last = episodeTransitions.lastOrNull() ?: return
        last.r = reward
        last.sNext = snap(nextBoard)
    }

    override fun refine(iGameResult: Int) {
        if (episodeTransitions.isEmpty()) return

        // 1) 原始資料更新
        applyUpdates(episodeTransitions, iGameResult)

        // 2) 旋轉資料增強：90/180/270（每局多 3 倍 transitions）
        val maps = arrayOf(ROT90, ROT180, ROT270)
        for (m in maps) {
            val rotated = episodeTransitions.map { st ->
                Step(
                    s = rotateBoard(st.s, m),
                    a = rotateAction(st.a, m),
                    r = st.r,
                    sNext = rotateBoard(st.sNext, m)
                )
            }
            applyUpdates(rotated, iGameResult)
        }

        episodeTransitions.clear()
    }

    private fun applyUpdates(steps: List<Step>, iGameResult: Int) {
        var nextMaxQ = when (iGameResult) {
            myType -> 1.0
            0 -> 0.0
            else -> -1.0
        }

        for (i in steps.indices.reversed()) {
            val st = steps[i]
            val sKey = brain.keyOf(st.s)
            val sNextKey = brain.keyOf(st.sNext)

            val qValues = getQValues(sKey)
            val oldQ = qValues[st.a]

            val tdTarget = st.r + gamma * nextMaxQ
            val tdError = tdTarget - oldQ
            qValues[st.a] += alpha * tdError

            nextMaxQ = getQValues(sNextKey).maxOrNull() ?: 0.0
        }
    }

    override fun resetForGame() {
        episodeTransitions.clear()
    }

    override fun resetForForget() {
        brain.clear()
        episodeTransitions.clear()
    }

    fun decayEpsilon(decayRate: Double = 0.999, minEpsilon: Double = 0.01) {
        epsilon *= decayRate
        epsilon = max(minEpsilon, epsilon)
    }

    fun <T> withEpsilon(temp: Double, block: () -> T): T {
        val old = epsilon
        epsilon = temp
        return try {
            block()
        } finally {
            epsilon = old
        }
    }
}

// ===== Symmetry augmentation (rotations) =====
// Board index assumed row-major:
// 0 1 2
// 3 4 5
// 6 7 8
// ROT90 maps oldPos -> newPos for clockwise rotation
private val ROT90 = intArrayOf(
    2, 5, 8,
    1, 4, 7,
    0, 3, 6
)

private fun compose(a: IntArray, b: IntArray): IntArray {
    val r = IntArray(9)
    for (i in 0..8) r[i] = b[a[i]]
    return r
}

private val ROT180 = compose(ROT90, ROT90)
private val ROT270 = compose(ROT180, ROT90)

private fun rotateAction(pos: Int, map: IntArray): Int = map[pos]

private fun rotateBoard(b: BoardStatus, map: IntArray): BoardStatus {
    val src = b.copyArray()
    val dst = IntArray(9)
    for (old in 0..8) {
        dst[map[old]] = src[old]
    }
    return BoardStatus(dst)
}

private fun snap(b: BoardStatus): BoardStatus = BoardStatus(b.copyArray())

private data class Step(
    val s: BoardStatus,
    val a: Int,
    var r: Double,
    var sNext: BoardStatus
)
