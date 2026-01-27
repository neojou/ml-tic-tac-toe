package com.neojou

import kotlin.random.Random

/**
 * PTable: for a fixed 3x3 board, action space is 0..8.
 * weights[pos] == 0 means illegal/unavailable.
 */
class PTable(
    var hasBuilt: Boolean = false,
    private val weights: IntArray = IntArray(9) { 0 },
) {
    private var totalWeight: Int = 0

    fun clear() {
        for (i in 0 until 9) weights[i] = 0
        totalWeight = 0
        hasBuilt = false
    }

    fun build(legalPos: Iterable<Int>, defaultWeight: Int = 1) {
        require(defaultWeight >= 0)
        clear()
        for (p in legalPos) {
            require(p in 0..8)
            weights[p] = defaultWeight
            totalWeight += defaultWeight
        }
        hasBuilt = true
    }

    fun getWeight(pos: Int): Int {
        require(pos in 0..8)
        return weights[pos]
    }

    fun total(): Int = totalWeight

    fun isLegal(pos: Int): Boolean {
        require(pos in 0..8)
        return weights[pos] > 0
    }

    fun setWeight(pos: Int, newWeight: Int) {
        require(pos in 0..8)
        require(newWeight >= 0)
        val old = weights[pos]
        weights[pos] = newWeight
        totalWeight += (newWeight - old)
    }

    /**
     * Useful for your current "default=1, chosen=2" logic:
     * never downgrade a weight accidentally.
     */
    fun setWeightMax(pos: Int, newWeight: Int) {
        val old = getWeight(pos)
        if (newWeight > old) setWeight(pos, newWeight)
    }

    fun addWeight(pos: Int, delta: Int) {
        val w = getWeight(pos)
        val nw = (w + delta).coerceAtLeast(0)
        setWeight(pos, nw)
    }

    /**
     * Weighted random sampling by weights (linear proportional).
     * Return null when totalWeight == 0 (no legal moves / not built / all zero).
     */
    fun sampleWeighted(random: Random = Random): Int? {
        val t = totalWeight
        if (t <= 0) return null
        val r = random.nextInt(t) // 0..t-1
        var acc = 0
        for (pos in 0 until 9) {
            val w = weights[pos]
            if (w <= 0) continue
            acc += w
            if (r < acc) return pos
        }
        return null
    }

    /**
     * Exploitation helper: choose argmax, break ties uniformly at random.
     */
    fun argMax(random: Random = Random): Int? {
        var bestPos: Int? = null
        var bestW = 0
        var tieCount = 0
        for (pos in 0 until 9) {
            val w = weights[pos]
            if (w <= 0) continue
            if (w > bestW) {
                bestW = w
                bestPos = pos
                tieCount = 1
            } else if (w == bestW && bestW > 0) {
                tieCount++
                if (random.nextInt(tieCount) == 0) bestPos = pos
            }
        }
        return bestPos
    }

    fun entries(): List<Pair<Int, Int>> =
        (0 until 9).mapNotNull { p ->
            val w = weights[p]
            if (w > 0) p to w else null
        }
}
