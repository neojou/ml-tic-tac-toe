package com.neojou

object SystemSettings {
    private var initialized = false

    fun initOnce() {
        if (initialized) return
        initialized = true

        // MyLog 初始設定
        MyLog.setGlobalMinLevel(LogLevel.INFO)
        //MyLog.setModuleMinLevel("AI", LogLevel.DEBUG)

        // 其他未來的全域設定也可以放這裡（需保持 commonMain 可用）
    }
}
