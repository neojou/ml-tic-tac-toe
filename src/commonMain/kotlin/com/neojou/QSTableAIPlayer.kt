package com.neojou

import kotlin.math.exp
import kotlin.random.Random

class QSTableAIPlayer(
    private val myType: Int = 2,
    private val temperature: Double = 1.0,
) : FirstEmptyAIPlayer() {

    val table = QSTable()
    val episode = Episode()

    private val oppType: Int = if (myType == 1) 2 else 1

    private val emptyS0 = BoardStatus(IntArray(9) { 0 })
    private var lastAfterMyMove: BoardStatus? = null

    private fun snapshot(s: BoardStatus): BoardStatus =
        BoardStatus(s.copyArray())

    override fun chooseMove(board: BoardStatus): Int? {

        // --- (A) 記錄對手那一步：S_prev -> board ---
        val sPrev = lastAfterMyMove ?: emptyS0
        val oppPos = tryGetSingleMovePosOrNull(sPrev, board)
        if (oppPos != null) {
            val saSnap = snapshot(sPrev)
            val legal = BoardAnalyze.getEmptyPosSet(sPrev)

            // 若尚未建立：把可下的都設成 1 分
            table.ensureBuilt(saSnap, legal, defaultWeight = 1)

            // 這裡一定要記 playerType=oppType，否則 refine 時無法分辨 O/X
            episode.append(saSnap, oppPos, legal, playerType = oppType)
        }

        // --- (B) 我方依 softmax(ptable) 選一步 ---
        val sa = snapshot(board)
        val legal2 = BoardAnalyze.getEmptyPosSet(board)
        if (legal2.isEmpty()) return null

        val pt = table.ensureBuilt(sa, legal2, defaultWeight = 1)

        val myPos = samplePosBySoftmax(pt, legal2, temperature) ?: return null

        episode.append(sa, myPos, legal2, playerType = myType)

        // --- (C) 更新 lastAfterMyMove ---
        lastAfterMyMove = BoardAnalyze.newBoardStatus(board, myPos, myType)
        return myPos
    }

    /**
     * 遊戲結束時呼叫：按你的規則加分，之後再印出紀錄
     */
    override fun refine(iGameResult: Int) {
        episode.refine(table, iGameResult)
    }

    fun resetEpisode() {
        table.clear()
        episode.clear()
        lastAfterMyMove = null
    }

    override fun showRecords() {
        table.show("QSTable")
        episode.show(table, "Episode")
    }

    override fun addLastMove(board: BoardStatus) {
        val sPrev = lastAfterMyMove ?: emptyS0
        val oppPos = tryGetSingleMovePosOrNull(sPrev, board) ?: return

        val saSnap = snapshot(sPrev)
        val legal = BoardAnalyze.getEmptyPosSet(sPrev)

        table.ensureBuilt(saSnap, legal, defaultWeight = 1)
        episode.append(saSnap, oppPos, legal, playerType = oppType)

        lastAfterMyMove = snapshot(board)
    }

    private fun tryGetSingleMovePosOrNull(sa: BoardStatus, sb: BoardStatus): Int? {
        return try {
            BoardAnalyze.getPos(sa, sb)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun samplePosBySoftmax(
        pt: PTable,
        legalPos: Set<Int>,
        temperature: Double,
        random: Random = Random,
    ): Int? {
        if (legalPos.isEmpty()) return null

        if (temperature <= 0.0) {
            val maxScore = legalPos.maxOf { pt.getWeight(it) }
            val ties = legalPos.filter { pt.getWeight(it) == maxScore }
            return ties[random.nextInt(ties.size)]
        }

        val scores: List<Pair<Int, Double>> =
            legalPos.map { pos -> pos to pt.getWeight(pos).toDouble() }

        val maxS = scores.maxOf { it.second }

        // softmax (max-shift for numerical stability), exp() in kotlin.math [web:67]
        val weights: List<Pair<Int, Double>> =
            scores.map { (pos, s) -> pos to exp((s - maxS) / temperature) }

        val sumW = weights.sumOf { it.second }
        if (!(sumW > 0.0) || sumW.isNaN() || sumW.isInfinite()) {
            val list = legalPos.toList()
            return list[random.nextInt(list.size)]
        }

        val r = random.nextDouble() * sumW
        var acc = 0.0
        for ((pos, w) in weights) {
            acc += w
            if (r < acc) return pos
        }
        return weights.last().first
    }
}
