import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Código compartilhado entre Android e iOS (Kotlin Multiplatform + Compose Multiplatform).
// O AGP 9 não permite mais combinar o plugin Kotlin Multiplatform com com.android.application no
// mesmo módulo, por isso o app Android continua em :app e depende deste módulo.
// Os targets iOS só compilam em macOS (no Windows/Linux o Gradle apenas os ignora) - o build iOS
// roda no CI (GitHub Actions macos-latest).
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "com.bismarck.voleimanager.shared"
        compileSdk = 37
        minSdk = 24
        // Roda os testes de commonTest na JVM (./gradlew :shared:testAndroidHostTest).
        withHostTest {}
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        commonMain.dependencies {
            implementation(libs.jetbrains.compose.runtime)
            // Anotações do Room (@Entity, @PrimaryKey...) nas entidades compartilhadas.
            api(libs.androidx.room.runtime)
        }
        iosMain.dependencies {
            // SQLite embarcado para o Room no iOS (no Android usa o SQLite do framework).
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// Compilador do Room (KSP) gera o AppDatabase_Impl / AppDatabaseConstructor para cada target.
dependencies {
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach {
        add(it, libs.androidx.room.compiler)
    }
}
