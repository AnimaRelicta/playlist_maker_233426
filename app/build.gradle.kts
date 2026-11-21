plugins {
    id("com.google.devtools.ksp")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "ru.student.playlistmaker"
    compileSdk = 35
    defaultConfig {
        applicationId = "ru.student.playlistmaker"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "1.6"
        resValue("string", "support_email", providers.gradleProperty("playlistMakerSupportEmail").getOrElse(""))
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}
dependencies {
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("io.insert-koin:koin-androidx-compose:4.0.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
}
