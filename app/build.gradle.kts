import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Carrega as credenciais do keystore de release a partir de keystore.properties (nunca commitado
// - veja keystore.properties.template). Isso garante que TODO build de release (seja pelo
// assistente "Generate Signed Bundle" do Android Studio, seja via linha de comando/CI) use
// sempre o mesmo arquivo e a mesma chave, evitando que uma versão publicada acabe assinada com
// um keystore diferente de uma anterior - cenário que quebra a atualização do app para quem já o
// instalou (Play Store exige o mesmo certificado de assinatura entre versões; um APK assinado
// com uma chave diferente da build anterior só instala se o usuário desinstalar a versão antiga).
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

// Telemetria opcional (Firebase Analytics + Crashlytics): os plugins do Firebase exigem um
// google-services.json válido (baixado do console Firebase) já na fase de configuração do Gradle,
// então só os aplicamos quando o arquivo existir localmente. Sem ele, o app builda normalmente e
// simplesmente não inclui a telemetria (TelemetryManager trata a ausência do Firebase).
val googleServicesFile = file("google-services.json")
val firebaseEnabled = googleServicesFile.exists()
if (firebaseEnabled) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

android {
    namespace = "com.bismarck.voleimanager.app"
    compileSdk = 37
    ndkVersion = "30.0.16138531"

    defaultConfig {
        applicationId = "com.bismarck.voleimanager.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "1.1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        localeFilters.addAll(listOf("en", "pt-rBR", "es"))
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Assina o build de debug com o mesmo keystore de release (quando disponível
            // localmente via keystore.properties) para evitar o conflito "assinatura diferente"
            // ao instalar builds de teste no mesmo aparelho que já tem a versão da Play Store.
            // Sem o arquivo (ex.: outro dev, CI), cai no keystore de debug padrão do Android.
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Navegação
    implementation(libs.androidx.navigation.compose)

    // Gson
    implementation(libs.google.gson)

    // Play In-App Review
    implementation(libs.play.review.ktx)

    // Play In-App Update
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)

    // Força versão mais nova de fragment (play-review-ktx traz 1.1.0 desatualizado como transitiva)
    implementation(libs.androidx.fragment)

    // Telemetria opcional (Firebase Analytics + Crashlytics) - ver TelemetryManager.
    // As dependências são sempre incluídas; sem um google-services.json local (plugins acima),
    // o Firebase simplesmente não inicializa e o TelemetryManager desativa a coleta com segurança.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}
