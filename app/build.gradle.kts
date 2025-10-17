import org.gradle.api.JavaVersion

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.pozoflix.firegramtv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pozoflix.firegramtv"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug { isMinifyEnabled = false }
    }

    // Habilita Compose y BuildConfig
    buildFeatures { compose = true; buildConfig = true }

    // Compilador de Compose compatible con Kotlin 1.9.25
    composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }

    // Compila Java y Kotlin contra JVM 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/**"
        }
    }
}

// Usa la toolchain 17 para Kotlin
kotlin { jvmToolchain(17) }

// Suprime el chequeo de compatibilidad indicando la versión de Kotlin requerida
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.9.25"
    )
}

// Fuerza todas las librerías de Kotlin a 1.9.25 para evitar mezclas de stdlib
configurations.all {
    resolutionStrategy {
        force(
            "org.jetbrains.kotlin:kotlin-stdlib:1.9.25",
            "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.25",
            "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.25",
            "org.jetbrains.kotlin:kotlin-reflect:1.9.25"
        )
    }
}

dependencies {
    // BOM de Compose (mantiene en sincronía las libs)
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.2")

    // ExoPlayer, Room, KSP, DataStore, Serialization, OkHttp, Retrofit, Moshi, Coroutines, WorkManager, etc.
    // (tal y como los tenías)

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.02"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("junit:junit:4.13.2")
}
