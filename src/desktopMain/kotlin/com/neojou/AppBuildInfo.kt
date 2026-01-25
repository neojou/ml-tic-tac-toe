package com.neojou

import java.util.Properties

object AppBuildInfo {
    private val props: Properties by lazy {
        Properties().apply {
            val stream = AppBuildInfo::class.java.classLoader
                .getResourceAsStream("build-info.properties")
                ?: return@apply
            stream.use { load(it) }
        }
    }

    val appName: String get() = props.getProperty("app.name", "ml-tic-tac-toe")
    val version: String get() = props.getProperty("app.version", "2026.1.25.1")
    val buildTime: String get() = props.getProperty("app.buildTime", "")

    fun asBuildInfo(): BuildInfo = BuildInfo(
        appName = appName,
        version = version,
        buildTime = buildTime
    )
}
