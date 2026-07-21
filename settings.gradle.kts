pluginManagement {
    plugins {
        id("org.jetbrains.compose") version "1.6.11"
        kotlin("jvm") version "1.9.24"
        kotlin("multiplatform") version "1.9.24"
        kotlin("plugin.serialization") version "1.9.24"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
rootProject.name = "FriedhofsenderApp"
include(":app")
include(":shared")
include(":desktop")