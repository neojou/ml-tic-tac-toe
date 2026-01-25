package com.neojou

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

object MySystemInfo {
    fun showJavaInfo() {
        // Java runtime info
        val javaVersion = System.getProperty("java.version")
        val javaVendor = System.getProperty("java.vendor")
        val javaRuntime = System.getProperty("java.runtime.version")

        MyLog.add("Java version: $javaVersion ($javaVendor)")
        MyLog.add("Java runtime: $javaRuntime")
    }

    fun showGradleInfo() {
        // Where the program is running from (important for reading gradle wrapper files)
        val cwd = Path.of("").toAbsolutePath()
        MyLog.add("Working directory: $cwd")

        // Gradle wrapper info (reads gradle/wrapper/gradle-wrapper.properties)
        val wrapperPropsPath = cwd.resolve("gradle/wrapper/gradle-wrapper.properties")
        val gradleVersion = runCatching {
            val props = Properties()
            Files.newInputStream(wrapperPropsPath).use { props.load(it) }  // Properties.load(InputStream) [web:51]
            val url = props.getProperty("distributionUrl") ?: return@runCatching null

            // Example: .../gradle-9.1.0-bin.zip  or  .../gradle-9.1.0-all.zip
            Regex("""gradle-(.+?)-(bin|all)\.zip""")
                .find(url)
                ?.groupValues
                ?.get(1)
        }.getOrNull()

        if (gradleVersion != null) {
            MyLog.add("Gradle (wrapper) version: $gradleVersion")
        } else {
            MyLog.add("Gradle (wrapper) version: <unknown> (cannot read $wrapperPropsPath)")
        }
    }

    fun showAll() {
        showJavaInfo()
        showGradleInfo()
    }
}
