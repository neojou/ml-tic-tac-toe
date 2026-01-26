import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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

    // NEW: wasmJs target (Compose Web / Kotlin-Wasm)
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        // 新寫法：Provider API
        outputModuleName.set("ml-tic-tac-toe")  // 或 "ml_tic_tac_toe" 比較保守
        browser { }
        binaries.executable()
    }

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

        // NEW
        val wasmJsMain by getting
    }
}

compose.desktop {
    application {
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

// 原本只有 desktopMain 吃到 build-info；NEW：web 也吃到（至少讓 web 可取得相同檔）
// 你後續若改成 expect/actual 讀取策略，也可以不用在 web 放這份，但先求跑起來很實用。
kotlin.sourceSets.named("desktopMain") { resources.srcDir(buildInfoDir) }
kotlin.sourceSets.named("wasmJsMain") { resources.srcDir(buildInfoDir) }  // NEW

// Gradle 9.x：宣告 task 依賴（desktop + wasm）
tasks.named<ProcessResources>("desktopProcessResources") { dependsOn(genBuildInfo) }
tasks.named<ProcessResources>("processDesktopMainResources") { dependsOn(genBuildInfo) }
tasks.named("compileKotlinDesktop") { dependsOn(genBuildInfo) }

// NEW：wasm 相關資源/編譯也依賴 buildInfo
tasks.matching { it.name.contains("wasm", ignoreCase = true) && it.name.contains("ProcessResources", ignoreCase = true) }
    .configureEach { dependsOn(genBuildInfo) }
tasks.matching { it.name.contains("wasm", ignoreCase = true) && it.name.contains("compileKotlin", ignoreCase = true) }
    .configureEach { dependsOn(genBuildInfo) }
