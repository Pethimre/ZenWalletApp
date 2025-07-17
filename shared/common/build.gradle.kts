import java.util.Properties
import kotlin.apply

plugins {
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"

    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.aestroon.common"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24

        val properties = Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }

        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")

        buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"${properties.getProperty("EXCHANGE_RATE_API_KEY")}\"")
        buildConfigField("String", "EXCHANGE_RATE_API_URL", "\"${properties.getProperty("EXCHANGE_RATE_API_URL")}\"")
        buildConfigField("String", "ALPHA_VANTAGE_API_KEY", "\"${properties.getProperty("ALPHA_VANTAGE_API_KEY")}\"")
        buildConfigField("String", "COINGECKO_API_KEY", "\"${properties.getProperty("COINGECKO_API_KEY")}\"")

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
}

dependencies {

    implementation(libs.jsoup)
    implementation(libs.lottie.compose)
    implementation(libs.androidx.biometric)

    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    // Supabase setup
    implementation("androidx.room:room-runtime:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")

    implementation(platform("io.github.jan-tennert.supabase:bom:2.4.0"))
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation(libs.gotrue.kt)
    implementation(libs.postgrest.kt)
    implementation(libs.realtime.kt)

    implementation(libs.androidx.security.crypto)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.utils)
    implementation(libs.ktor.client.core)
    implementation(libs.coil.compose)

    // For instrumentation tests
    implementation(libs.play.services.auth)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

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
}