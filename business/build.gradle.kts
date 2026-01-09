plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)  // Changed from kapt
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.uyscuti.social.business"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
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
    implementation(libs.androidx.swiperefreshlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room - Changed from kapt to ksp
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.rxjava3)
    ksp(libs.androidx.room.compiler)

    // Networking
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Image loading - Changed from kapt to ksp
    implementation("com.github.bumptech.glide:glide:4.15.1")
    ksp("com.github.bumptech.glide:compiler:4.15.1")

    // UI components
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.tbuonomo:dotsindicator:5.0")
    implementation("io.getstream:photoview:1.0.2")

    // Media
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation("androidx.media3:media3-extractor:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")

    // Google services
    implementation(libs.play.services.location)

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Hilt - Changed from kapt to ksp
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Animations
    implementation("com.daimajia.easing:library:2.4@aar")
    implementation("com.daimajia.androidanimations:library:2.4@aar")
    implementation("com.facebook.shimmer:shimmer:0.1.0@aar")

    // Emoji
    implementation("com.vanniktech:emoji-google-compat:0.21.0")

    // Document handling
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("org.apache.poi:poi-scratchpad:5.2.4")

    // Image compression
    implementation("id.zelory:compressor:3.0.1")

    // Project modules
    implementation(project(":app"))
    implementation(project(":chatsuit"))
    implementation(project(":network"))
    implementation(project(":core"))
}