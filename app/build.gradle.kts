plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    // ✅ añade este:
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
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Compose BOM (mantiene versiones compatibles entre sí) ---
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    // --- Compose Core ---
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")

    // --- Material 3 ---
    implementation("androidx.compose.material3:material3")

    // --- Navigation Compose ---
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // --- Activity Compose (para setContent en MainActivity) ---
    implementation("androidx.activity:activity-compose:1.9.2")

    // --- Kotlin Standard Library ---
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")

    // --- Debug / Preview ---
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Testing ---
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("junit:junit:4.13.2")
}
