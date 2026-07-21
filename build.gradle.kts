// Root build.gradle.kts

plugins {
    // Keine Android- oder Kotlin-Plugins hier!
    // Alles wird im Modul (app/build.gradle.kts) angewendet.
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
