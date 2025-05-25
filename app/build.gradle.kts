plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.vlog.player"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vlog.player"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // Moshi
    implementation(libs.squareup.moshi)
    implementation(libs.squareup.moshi.kotlin)
    // Coil Image
    implementation(libs.coil.kt.compose)
    // navigation
    implementation(libs.androidx.navigation.compose)
    // iconsExtended
    implementation(libs.androidx.compose.material.iconsExtended)
    // media3
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.transformer)


    // Testing dependencies
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline) // For mocking final classes/methods if needed
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.ext.junit.ktx) // For AndroidX JUnit runner extensions
    testImplementation(libs.androidx.media3.test.utils) // For Media3 testing utilities
    testImplementation(libs.androidx.arch.core.testing) // For InstantTaskExecutorRule, useful for LiveData/ViewModels
    testImplementation(libs.kotlinx.coroutines.test) // For testing coroutines
}