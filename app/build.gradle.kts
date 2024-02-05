plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "ru.otus.geo"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.otus.geo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    kapt {
        correctErrorTypes = true
    }

    flavorDimensions.add("geoTarget")
    productFlavors {
        // Uses mock location data.
        create("mock") {
            dimension = "geoTarget"
        }
        // Uses GMS location data.
        create("gms") {
            dimension = "geoTarget"
        }
    }
}

// A new configuration named mockImplementation is added to the project.
val mockImplementation by configurations
// A new configuration named mockImplementation is added to the project.
val gmsImplementation by configurations

dependencies {
    implementation(project(":domain"))
    implementation(project(":net"))

    // The mockImplementation configuration is used to declare a dependency on the geomock module.
    // Will be used only in the mock flavor.
    mockImplementation(project(":geomock"))
    // The gmsImplementation configuration is used to declare a dependency on the geogms module.
    // Will be used only in the GMS flavor.
    gmsImplementation(project(":geogms"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
}