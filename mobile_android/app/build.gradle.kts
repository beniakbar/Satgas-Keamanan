plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.satgaskeamanan.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.satgaskeamanan.app"
        minSdk = 24
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Library untuk koneksi HTTP (pilih salah satu: Volley atau Retrofit)
    // Direkomendasikan: Retrofit (modern dan kuat)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    // Library untuk memuat gambar dari URL (e.g., Glide)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    // Google Location Services (GPS)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // OkHttp (Penting untuk Interceptor dan log)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}
