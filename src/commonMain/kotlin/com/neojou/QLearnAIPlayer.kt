package com.neojou

import kotlin.math.max
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random


class QLearnAIPlayer(
    val myType: Int = 2,
    val brain: QLearnBrain,
    private val alpha: Double = 0.1,
    private val gamma: Double = 0.95,
    private var epsilon: Double = 0.1,
    private val random: Random = Random.Default
) : AIPlayer {

    private val episodeTransitions = mutableListOf<Quad<String, Int, Double, String>>()

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

        episodeTransitions.add(Quad(key, action, 0.0, ""))
        return action
    }

    fun recordStepOutcome(reward: Double, nextBoard: BoardStatus) {
        if (episodeTransitions.isEmpty()) return
        val lastIndex = episodeTransitions.lastIndex
        val last = episodeTransitions[lastIndex]
        val sNextKey = brain.keyOf(nextBoard)
        episodeTransitions[lastIndex] = Quad(last.first, last.second, reward, sNextKey)
    }

    override fun refine(iGameResult: Int) {
        if (episodeTransitions.isEmpty()) return

        var nextMaxQ = when (iGameResult) {
            myType -> 1.0
            0 -> 0.0
            else -> -1.0
        }

        for (i in episodeTransitions.indices.reversed()) {
            val (s, a, r, sNext) = episodeTransitions[i]
            val qValues = getQValues(s)
            val oldQ = qValues[a]

            val tdTarget = r + gamma * nextMaxQ
            val tdError = tdTarget - oldQ
            qValues[a] += alpha * tdError

            nextMaxQ = getQValues(sNext).maxOrNull() ?: 0.0
        }

        episodeTransitions.clear()
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

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
