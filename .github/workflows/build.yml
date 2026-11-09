plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.materialclock"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.materialclock"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    val releaseStore = (findProperty("clockStoreFile") as String?)
        ?.let(::file)
        ?.takeIf { it.exists() }

    if (releaseStore != null) {
        signingConfigs.create("release") {
            storeFile = releaseStore
            storePassword = findProperty("clockStorePassword") as String?
            keyAlias = findProperty("clockKeyAlias") as String?
            keyPassword = findProperty("clockKeyPassword") as String?
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseStore != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            setIsDebuggable(true)
        }
    }
}
