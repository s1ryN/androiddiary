plugins {
    id("com.android.application")
    // Protože kód je v Javě, Kotlin plugin nepoužíváme.
}

android {
    namespace = "com.example.denik"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.denik"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Java 8+ pro kompatibilitu s CameraX a lambdami
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
}

dependencies {
    // Základní Android knihovny
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.room:room-runtime:2.5.2")
    annotationProcessor ("androidx.room:room-compiler:2.5.2")
    implementation(libs.concurrent.futures)


    // Přidána závislost na androidx.concurrent, která poskytuje ListenableFuture
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.activity)
    implementation(libs.play.services.location)

    // Testovací knihovny (volitelně)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
