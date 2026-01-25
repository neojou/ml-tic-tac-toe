package com.neojou

import java.awt.Desktop

object MyMacOSIntegration {
    fun installAboutHandler(onAbout: () -> Unit) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler { onAbout() }
        }
    }

    fun clearAboutHandler() {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(null) // revert to default behavior [web:275]
        }
    }
}
