import java.awt.Desktop

object MyMacOSIntegration {

    fun installAboutHandler() {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler {
                MyAbout.show(null) // 用你既有的 About 對話框內容/格式
            }
        }
    }
}
