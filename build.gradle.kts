import java.util.Locale
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "2.2.21"
}

group = "com.neojou"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}

val genBuildInfo by tasks.registering {
    val outDir = layout.buildDirectory.dir("generated/resources/buildInfo")
    outputs.dir(outDir)

    doLast {
        val dir = outDir.get().asFile
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


tasks.processResources {
    dependsOn(genBuildInfo)
    from(layout.buildDirectory.dir("generated/resources/buildInfo"))
}

