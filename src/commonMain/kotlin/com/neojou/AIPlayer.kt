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
}
