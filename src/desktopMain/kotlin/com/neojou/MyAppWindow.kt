package com.neojou


import java.awt.GridBagLayout
import javax.swing.*

object MyAppWindow {
    fun show() {
        val frame = JFrame(AppBuildInfo.appName)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        // 中央兩行字（上下左右置中）
        val label = JLabel("<html><div style='text-align:center;'>Hello World<br/>2026</div></html>")
        label.horizontalAlignment = SwingConstants.CENTER

        val panel = JPanel(GridBagLayout())
        panel.add(label)
        frame.contentPane = panel

        frame.setSize(420, 260)
        frame.setLocationRelativeTo(null) // 視窗置中
        frame.isVisible = true
    }
}
