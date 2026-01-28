package com.neojou

interface AIPlayer {

    /** @return 要下的位置 0..8；若無可下則回傳 null */
    fun chooseMove(board: BoardStatus): Int?

    fun showRecords() { return }

    /**
     * 用於補記「最後一步」：當遊戲結束導致不會再呼叫 chooseMove 時，由外部傳入最後盤面。
     * 預設不做事；有記錄功能的 AI 再 override。
     */
    fun addLastMove(board: BoardStatus) { return }

    fun refine(iGameResult: Int) { return }

    /**
     * 清空所有記錄，包括 QSTable、episode 和 lastAfterMyMove。
     * 預設不做事；有記錄功能的 AI 再 override。
     */
    fun clearRecords() { return }

    /**
     * 新遊戲時呼叫：清 episode 和 lastAfterMyMove，保留 QSTable。
     * 預設不做事；有記錄功能的 AI 再 override。
     */
    fun resetForGame() { return }

    /**
     * Forget 時呼叫：清 QSTable、episode 和 lastAfterMyMove。
     * 預設不做事；有記錄功能的 AI 再 override。
     */
    fun resetForForget() { return }
}