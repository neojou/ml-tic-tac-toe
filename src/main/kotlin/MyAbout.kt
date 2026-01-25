import javax.swing.JOptionPane
import java.awt.Component

object MyAbout {
    fun show(parent: Component? = null) {
        val msg = buildString {
            appendLine("${AppBuildInfo.appName}  ${AppBuildInfo.version}")
            append("Build on ${AppBuildInfo.buildTime}")
        }
        JOptionPane.showMessageDialog(parent, msg, "About", JOptionPane.INFORMATION_MESSAGE)
    }
}
