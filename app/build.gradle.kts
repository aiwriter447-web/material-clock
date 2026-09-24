plugins {
    // No `kotlin.android`: AGP 9 has built-in Kotlin support and applying it is a hard error.
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

    /**
     * Release signing, with the key kept out of the repository.
     *
     * The four properties live in `~/.gradle/gradle.properties`, not here and not in the project's
     * own `gradle.properties`, so nothing checked in can leak them. When they are absent the
     * release build falls back to the debug key: that still produces an installable APK for
     * sideloading a personal build, and it is deliberately useless for distribution, because a
     * debug-keyed APK cannot be updated by a properly signed one later.
     */
    val releaseStore = (findProperty("clockStoreFile") as String?)?.let(::file)?.takeIf { it.exists() }
    if (releaseStore != null) {
        signingConfigs.create("release") {
            storeFile = releaseStore
            storePassword = findProperty("clockStorePassword") as String?
            keyAlias = findProperty("clockKeyAlias") as String?
            keyPassword = findProperty("clockKeyPassword") as String?
        }
    }

    /**
     * A fixed debug key, checked into the repo, instead of AGP's implicit one.
     *
     * Without this, every fresh checkout — and every GitHub Actions run, since the runner is a
     * new machine each time — gets its own auto-generated `~/.android/debug.keystore` with a
     * different key. Android refuses to install an APK over an existing app unless the two are
     * signed with the same certificate, so a debug APK built on CI cannot update the previous
     * one: it fails with "App not installed", and the only way past that is uninstalling first,
     * which erases the app's alarms, cities and settings. Pinning the debug key here means every
     * CI build is signed the same way, so a new APK always updates in place over the old one.
     */
    signingConfigs.getByName("debug") {
        storeFile = file("debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                // Must be the -optimize variant; plain proguard-android.txt carries -dontoptimize,
                // which is a hard error on AGP 9.
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // `material-icons-extended` alone ships several thousand vector icons; unshrunk, that
            // is tens of MB of dead weight sitting next to the handful this app actually draws.
            // Debug had neither shrink flag, so every debug build carried the whole library (and
            // everything else's unused code) uncut — the R8/resource pass below is what release
            // was already getting. `isDebuggable` stays implicit-true for a `debug` buildType, so
            // this does not turn the build into something that can't be attached to or logged.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        localeFilters += listOf("en")
    }

    // No accounts, no telemetry, no ad IDs. The dependency manifest stays out of the APK too.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/*.version",
                "/META-INF/com.android.tools/**",
                "/kotlin/**",
                "/DebugProbesKt.bin",
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        // [M3E] Most expressive components are still opt-in even though MaterialExpressiveTheme
        // itself is not. Opting in centrally keeps the annotation noise out of every call site.
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.ui.text.ExperimentalTextApi",
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.graphics.shapes)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.androidx.test.core)
}
