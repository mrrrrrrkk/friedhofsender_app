plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("jvmMain") {
        withJava()
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                // Zugriff auf Logik und Data Models aus dem shared-Modul
                implementation(project(":shared"))

                // Compose Desktop UI
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.foundation)
                implementation(compose.ui)

                // Coroutines für Desktop
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
            }
        }
    }
}

// Konfiguration für das Erstellen von nativen Windows- (.exe/.msi) und Linux-Paketen (.deb/.rpm)
compose.desktop {
    application {
        mainClass = "de.friedhofsender.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, // Windows Installer
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe, // Windows Portable/Exe
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb  // Linux Debian/Ubuntu
            )
            packageName = "FriedhofsenderDesktop"
            packageVersion = "1.0.0"
            description = "Friedhofsender Audio Companion für Windows & Linux"
            copyright = "© 2026 Friedhofsender"

            // System-Tray & Fenster-Icon (kannst du später anpassen)
            // windows {
            //     iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
            // }
            // linux {
            //     iconFile.set(project.file("src/jvmMain/resources/icon.png"))
            // }
        }
    }
}