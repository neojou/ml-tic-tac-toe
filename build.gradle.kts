import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    kotlin("multiplatform") version "2.2.21"
    id("org.jetbrains.compose") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
}

group = "com.neojou"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm("desktop")
    jvmToolchain(24)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
        val commonTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        // 若你後續把 Main.kt 改成有 package，例如 com.neojou.MainKt，這裡也要跟著改
        mainClass = "com.neojou.MainKt"
    }
}

// ---------- build-info.properties (generated resource) ----------
val buildInfoDir = layout.buildDirectory.dir("generated/resources/buildInfo")

val genBuildInfo by tasks.registering {
    outputs.dir(buildInfoDir)
    doLast {
        val dir = buildInfoDir.get().asFile
        dir.mkdirs()

        val buildTime = ZonedDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm MMMM d, yyyy", Locale.ENGLISH)
        )

        file("${dir.absolutePath}/build-info.properties").writeText(
            "app.name=ml-tic-tac-toe\n" +
                    "app.version=2026.1.25.1\n" +
                    "app.buildTime=$buildTime\n"
        )
    }
}

// 讓 desktopMain 的 resources 包含 buildInfoDir
kotlin.sourceSets.named("desktopMain") {
    resources.srcDir(buildInfoDir)
}

// Gradle 9.x：明確宣告 consumer tasks 依賴 producer task，避免 implicit dependency 驗證失敗
tasks.named<ProcessResources>("desktopProcessResources") {
    dependsOn(genBuildInfo)
}
tasks.named<ProcessResources>("processDesktopMainResources") {
    dependsOn(genBuildInfo)
}
tasks.named("compileKotlinDesktop") {
    dependsOn(genBuildInfo)
}
