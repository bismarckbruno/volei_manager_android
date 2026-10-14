// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    // Kotlin/Compose Multiplatform - módulo :shared (código comum Android + iOS)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    // Aplicados condicionalmente em app/build.gradle.kts, somente quando google-services.json
    // existir (veja o mesmo padrão usado para keystore.properties).
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}