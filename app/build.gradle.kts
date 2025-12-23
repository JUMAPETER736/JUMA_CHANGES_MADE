plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.uyscuti.social.circuit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.uyscuti.social.circuit"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
            excludes += "mozilla/public-suffix-list.txt"
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.animation.graphics.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.core.animation)
    implementation(libs.appcompat)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.ui.graphics)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.filament.android)
    implementation(libs.androidx.scenecore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.Spikeysanju:MotionToast:1.4")
    implementation("com.github.softrunapp:Paginated-RecyclerView:1.1.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity:1.9.0")

    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    implementation("androidx.browser:browser:1.7.0")
    //noinspection GradleCompatible
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("io.github.nikartm:image-support:2.0.0")

    // Image loading libraries
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    // Emoji
    implementation("com.vanniktech:emoji-google-compat:0.18.0")
    implementation("com.vanniktech:emoji-twitter:0.18.0")
    implementation("com.vanniktech:emoji-facebook:0.18.0")
    implementation("com.vanniktech:emoji-google:0.18.0")
    implementation("com.vanniktech:emoji-ios:0.18.0")

    // Room database
    implementation("androidx.room:room-runtime:2.6.0-alpha02")
    implementation("androidx.room:room-ktx:2.6.0-alpha02")
    kapt("androidx.room:room-compiler:2.6.0-alpha02")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.guolindev.permissionx:permissionx:1.6.1")

    val work_version = "2.9.0"
    implementation("io.socket:socket.io-client:2.0.0")
    implementation("androidx.work:work-runtime-ktx:$work_version")
    implementation("androidx.hilt:hilt-work:1.1.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.daimajia.easing:library:2.4@aar")
    implementation("com.daimajia.androidanimations:library:2.4@aar")

    // Media3 - NEW
    val mediaVersion = "1.3.1"
    implementation("androidx.media3:media3-exoplayer:$mediaVersion")
    implementation("androidx.media3:media3-ui:$mediaVersion")
    implementation("androidx.media3:media3-exoplayer-dash:$mediaVersion")
    implementation("androidx.media3:media3-common:$mediaVersion")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    implementation("com.github.colourmoon:readmore-textview:v1.0.2")
    implementation("androidx.paging:paging-common-ktx:3.1.1")
    implementation("androidx.paging:paging-runtime-ktx:3.1.1")

    implementation("com.github.alxrm:audiowave-progressbar:0.9.2")
    implementation("com.facebook.shimmer:shimmer:0.1.0@aar")
    implementation("com.github.ZeroOneZeroR:android_audio_mixer:v1.1")
    implementation("com.github.lincollincol:amplituda:2.2.2") // or newer

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("org.apache.poi:poi-scratchpad:5.2.4")

    implementation("id.zelory:compressor:3.0.1")

    implementation ("com.tbuonomo:dotsindicator:5.0")

    implementation(libs.expandabletextview)
    implementation("com.github.yuzumone:ExpandableTextView:0.3.2")
    implementation("io.github.afreakyelf:Pdf-Viewer:2.1.1")
    implementation("me.relex:circleindicator:2.1.6")

    implementation ("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.facebook.android:facebook-login:16.2.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Project modules
    implementation(project(":network"))
    implementation(project(":compressor"))
    implementation(project(":notifications"))
    implementation(project(":core"))
    implementation(project(":business"))
    implementation(project(":call"))
    implementation(project(":chatsuit"))
    implementation(project(":medialoader"))
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

}