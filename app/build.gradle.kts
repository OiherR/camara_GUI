plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.camara_gui"
    compileSdk {
        version = release(36)
    }
    flavorDimensions += "version"

    productFlavors {
        create("flavour1") {
            dimension = "version"
            applicationId = "com.example.camara_gui_android1"
        }

        create("flavour2") {
            dimension = "version"
            applicationId = "com.example.camara_gui_android2"
        }

        create("flavour3") {
            dimension = "version"
            applicationId = "com.example.camara_gui_android3"
        }
    }
    defaultConfig {
        applicationId = "com.example.camara_gui"
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
}