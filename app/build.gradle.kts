import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Release signing credentials are kept out of version control: they come from
// keystore.properties (gitignored) or, for CI, from environment variables.
// See keystore.properties.example.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun signingValue(key: String, env: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(env)

android {
    namespace = "com.meddiktat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.meddiktat"
        // minSdk 26 (Android 8.0): adaptive icons + MediaRecorder pause/resume available.
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val store = signingValue("storeFile", "MEDDIKTAT_STORE_FILE")
            // Left unconfigured when no credentials are available; the release
            // build then falls back to producing an unsigned APK.
            if (store != null) {
                storeFile = rootProject.file(store)
                storePassword = signingValue("storePassword", "MEDDIKTAT_STORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "MEDDIKTAT_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "MEDDIKTAT_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Pins Java and Kotlin compilation to a JDK 17 toolchain, independent of the
// JVM the Gradle daemon happens to run on. Without this, Gradle falls back to
// the daemon's JVM and the build fails if that is a JRE without javac.
kotlin {
    jvmToolchain(17)
}

// The Hilt plugin creates its own JavaCompile task, which does not inherit the
// toolchain above and would fall back to the daemon's JVM. Pin every
// JavaCompile task explicitly so third-party plugins are covered too.
val javaToolchains = extensions.getByType(JavaToolchainService::class.java)
tasks.withType(JavaCompile::class.java).configureEach {
    javaCompiler.set(
        javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Lifecycle + Compose
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM keeps all Compose artifacts on a single, compatible version.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room (metadata persistence)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt (dependency injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
