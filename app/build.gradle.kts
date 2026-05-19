plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.ksp") version "1.9.22"
}

android {
    namespace = "com.example.todo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.todo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildFeatures {
            compose = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.14"
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose (pin explicit versions compatible with both)
    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("androidx.compose.ui:ui-graphics:1.6.7")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
    implementation("androidx.compose.material3:material3:1.2.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Glance AppWidget
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-appwidget-proto:1.0.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Gson (List<Int> TypeConverter)
    implementation("com.google.code.gson:gson:2.10.1")

    // Java time backport (Android below API 26 via desugar)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
    // Guava (required by Glance 1.0 internally)
    implementation("com.google.guava:guava:33.2.1-android")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testAnnotationProcessor("androidx.room:room-compiler:2.6.1")
}
