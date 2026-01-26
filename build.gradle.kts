import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.2.21"
    id("org.jetbrains.compose") version "1.10.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
}

group = "com.neojou"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js")
}

kotlin {
    jvm("desktop")
    jvmToolchain(24)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("ml-tic-tac-toe")
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
        val wasmJsMain by getting {
            // NEW: 移除 implementation(kotlin("js"))，用 raw js() 替代
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.neojou.MainKt"
    }
}