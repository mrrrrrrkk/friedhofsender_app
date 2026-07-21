plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.6.11"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

dependencies {
    // Shared Logik-Modul verknüpfen
    implementation(project(":shared"))

    // Compose Desktop UI
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.foundation)
    implementation(compose.ui)

    // Coroutines Swing Dispatcher
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
}

compose.desktop {
    application {
        mainClass = "de.friedhofsender.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "FriedhofsenderDesktop"
            packageVersion = "1.0.0"
            description = "Friedhofsender Audio Companion"
            copyright = "© 2026 Friedhofsender"
        }
    }
}