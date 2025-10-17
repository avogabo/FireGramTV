import org.gradle.api.JavaVersion

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    // Plugin de kotlinx.serialization (necesario por @Serializable en ChannelCfg, etc.)
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
            // Si quieres ofuscar en release, pon true y deja el proguard:
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    // Compose
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        // Alineado con Kotlin 1.9.24 y Compose BOM 2024.09.02
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    // Unifica Java/Kotlin en JVM 17 (evita el error de KSP vs javac)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/**"
        }
    }
}

// Fuerza toolchain de Kotlin a Java 17
kotlin {
    jvmToolchain(17)
}

dependencies {
    // --- Compose BOM (mantiene versiones compatibles entre sí) ---
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))

    // --- Compose Core ---
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")

    // --- Material 3 (sin versión porque la gestiona el BOM) ---
    implementation("androidx.compose.material3:material3")

    // --- Navigation Compose ---
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // --- Activity Compose ---
    implementation("androidx.activity:activity-compose:1.9.2")

    // --- ExoPlayer ---
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:extension-mediasession:2.19.1")

    // --- Imágenes ---
    implementation("io.coil-kt:coil-compose:2.7.0")

    // --- Room + KSP ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // --- DataStore ---
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // --- Kotlinx Serialization ---
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // --- Networking (OkHttp + Retrofit + Moshi) ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // --- WorkManager ---
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // --- Debug / Preview ---
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Testing ---
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.02"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("junit:junit:4.13.2")
}
