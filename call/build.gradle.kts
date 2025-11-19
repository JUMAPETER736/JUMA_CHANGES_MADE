plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    kotlin("kapt")

}

android {
    namespace = "com.uyscuti.social.call"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.mesibo.api:webrtc:1.0.5")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    implementation("com.google.code.gson:gson:2.10.1") // kept latest 2.10.1
    implementation("com.guolindev.permissionx:permissionx:1.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.3.1")

    implementation("androidx.room:room-runtime:2.6.0-alpha02")
    implementation("androidx.room:room-ktx:2.6.0-alpha02")
    kapt("androidx.room:room-compiler:2.6.0-alpha02")

    implementation("com.google.dagger:hilt-android:2.51.1") // latest version
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("com.github.bumptech.glide:glide:4.15.1") // latest version

    implementation("io.socket:socket.io-client:2.0.0")

    implementation(project(":core"))
}