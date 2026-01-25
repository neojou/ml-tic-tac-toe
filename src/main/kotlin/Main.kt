import javax.swing.SwingUtilities

private fun setupMacAppName() {
    // 必須在建立任何 Swing/AWT 視窗前設定，才會影響 macOS 左上角的應用程式名稱 [web:106]
    System.setProperty("apple.awt.application.name", AppBuildInfo.appName) // 例如 Hello2026 [web:105]
}

fun main() {
    MySystemInfo.showAll()

    setupMacAppName()
    MyMacOSIntegration.installAboutHandler()

    // 啟動 GUI（Swing 建議在 EDT 上執行）
    SwingUtilities.invokeLater {
        MyAppWindow.show()
    }
}

