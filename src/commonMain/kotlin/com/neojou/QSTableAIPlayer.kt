package com.neojou

import kotlin.math.exp
import kotlin.math.max
import kotlin.random.Random

// MOD: 建構子新增 table 參數 (必填)
class QSTableAIPlayer(
    val myType: Int = 2,
    private val table: QSTable, // NEW: 外部注入，支持共享
    private val temperature: Double = 1.0, // 初始 temperature (可忽略，動態計算會覆蓋)
) : FirstEmptyAIPlayer() {

    var globalGames = 0 // 改為 var，讓 refine 遞增

    val episode = Episode()

    private val oppType: Int = if (myType == 1) 2 else 1

    private val emptyS0 = BoardStatus(IntArray(9) { 0 })
    private var lastAfterMyMove: BoardStatus? = null

    private fun snapshot(s: BoardStatus): BoardStatus =
        BoardStatus(s.copyArray())

    override fun chooseMove(board: BoardStatus): Int? {

        // 新增：計算動態 temperature (衰減探索：從 1.0 降到 0.1，基於全域遊戲計數)
        val currentTemp = max(0.1, 1.0 - globalGames / 10000.0)

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

        //val myPos = samplePosBySoftmax(pt, legal2, temperature) ?: return null
        val myPos = samplePosByLinear(pt, legal2, currentTemp) ?: return null // 新增：用動態 currentTemp

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
        var currentEpisode = episode
        repeat(3) { _ ->  // + 3 旋轉
            currentEpisode = currentEpisode.clockwise()
            currentEpisode.refine(table, iGameResult)
        }
        globalGames++  // 每局結束後全域計數 +1 (觸發 temperature decay)
    }

    // 移除 resetEpisode()，取代為以下兩個新方法

    // 新增：新遊戲時重置單局狀態，保留 QSTable
    override fun resetForGame() {
        episode.clear()
        lastAfterMyMove = null
        //MyLog.add("Reset for new game: cleared episode and lastAfterMyMove (QSTable preserved)")
    }

    // 新增：Forget 時重置所有學習
    override fun resetForForget() {
        table.clear()
        episode.clear()
        lastAfterMyMove = null
        globalGames = 0  // 新增：Forget 時重置計數 (可選，依需求)
        MyLog.add("Reset for forget: cleared QSTable, episode, and lastAfterMyMove")
    }

    // 更新：clearRecords() 現在呼叫 resetForForget()
    override fun clearRecords() {
        resetForForget()
    }

    override fun showRecords() {
        table.show("QSTable")
        episode.show(table, "Episode")
        MyLog.add("Global games played: $globalGames (current temp: ${max(0.1, 1.0 - globalGames / 10000.0)})")
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

    private fun samplePosByLinear(
        pt: PTable,
        legalPos: Set<Int>,
        temperature: Double, // 可選：用來縮放 score (e.g., score / temp)，但線性通常不需
        random: Random = Random,
    ): Int? {
        if (legalPos.isEmpty()) return null

        if (temperature <= 0.0) {
            // Greedy: 選 max score (與 softmax 相同)
            val maxScore = legalPos.maxOf { pt.getWeight(it) }
            val ties = legalPos.filter { pt.getWeight(it) == maxScore }
            return ties[random.nextInt(ties.size)]
        }

        // 計算總 score (只 legal pos)
        val totalScore = legalPos.sumOf { pt.getWeight(it).toDouble() }
        if (totalScore <= 0.0) {
            // Fallback: 純隨機
            val list = legalPos.toList()
            return list[random.nextInt(list.size)]
        }

        // 輪盤選擇：產生 r ∈ [0, totalScore)，累積 score 直到超過
        val r = random.nextDouble() * totalScore
        var acc = 0.0
        for (pos in legalPos.sorted()) {  // sorted 確保穩定順序
            val score = pt.getWeight(pos).toDouble()
            acc += score
            if (r < acc) return pos
        }
        return legalPos.last()  // Fallback
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

        // softmax (max-shift for numerical stability), exp() in kotlin.math
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