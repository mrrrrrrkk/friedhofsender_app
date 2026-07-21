plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}
dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.foundation)
    implementation(compose.ui)

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
            packageVersion = "1.0.1"
            vendor = "Friedhofsender"
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
            }
            description = "Friedhofsender Audio Companion"
            copyright = "© 2026 Friedhofsender"
        }
    }
}