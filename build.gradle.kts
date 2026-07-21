plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
    id("com.android.library") version "8.5.2" apply false
    kotlin("multiplatform") version "1.9.24" apply false
    kotlin("plugin.serialization") version "1.9.24" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
}
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}