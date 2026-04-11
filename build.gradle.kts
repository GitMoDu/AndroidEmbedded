import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.legacy.kapt)
}

android {
    namespace = "com.dogecoding.android_embedded"
    compileSdk = 36

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // MSAL Activity must include MSAL Activity in placeholder.
        // as well as msal.path=Base64-encoded-signature-hash in local.properties.
//        val properties = Properties()
//        val localPropertiesFile = project.rootProject.file("local.properties")
//        if (localPropertiesFile.exists()) {
//            localPropertiesFile.inputStream().use { properties.load(it) }
//        }
//        val msalPath = properties.getProperty("msal.path")
//            ?: throw GradleException("msal.path not found in local.properties")
//        manifestPlaceholders["msalPath"] = msalPath
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
        }
    }
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.androidx.activity)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.constraintlayout)
    api(libs.lottie)

    // Google Drive
    api(libs.google.auth)
    api(libs.google.api.client)
    api(libs.google.drive)
    api(libs.google.gson)
    api(libs.androidx.credentials)
    api(libs.androidx.credentials.play.services.auth)
    api(libs.googleid)

    api(libs.microsoft.msal) {
        exclude(group = "com.microsoft.device.display", module = "display-mask")
    }
    api(libs.microsoft.graph) {
        exclude(group = "com.microsoft.device.display", module = "display-mask")
    }

    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    add("kapt", libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    api(libs.usb.serial.for1.android)
    api(libs.nordic.ble)
    api(libs.nordic.ble.ktx)
    api(libs.androidx.security.crypto)
}
