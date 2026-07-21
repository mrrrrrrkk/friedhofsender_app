plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    // 1. Target: Android
    androidTarget()

    // 2. Target: Desktop (Windows, Linux, macOS)
    jvm("desktop")

    sourceSets {
        // Gemeinsamer Code für ALLE Plattformen (Windows, Linux, Android)
        commonMain.dependencies {
            // Coroutines für asynchronen Code
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            
            // Serialization für Groq JSON Responses
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // Ktor HTTP-Client (Plattformübergreifend)
            implementation("io.ktor:ktor-client-core:2.3.11")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
        }

        // Android-spezifische HTTP-Engine
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:2.3.11")
        }

        // Desktop-spezifische HTTP-Engine (Windows & Linux)
        getByName("desktopMain").dependencies {
            implementation("io.ktor:ktor-client-cio:2.3.11")
        }
    }
}

android {
    namespace = "de.friedhofsender.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}