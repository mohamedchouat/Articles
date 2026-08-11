plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "com.chtmed.restapidebugger"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // Exposes the "release" AAR variant as a publishable component, plus a
    // sources jar, so this module can be consumed as a Maven artifact (e.g.
    // via JitPack from other projects) instead of only as a project(":...") dep.
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// JitPack resolves com.github.<user>.<repo>:<module>:<tag> to whatever this
// module publishes to the local Maven repo, regardless of the exact
// groupId/version set here — but they're set explicitly anyway so
// `publishToMavenLocal` also works standalone for local testing.
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.mohamedchouat.Articles"
            artifactId = "rest-api-debugger"
            version = System.getenv("VERSION_NAME") ?: "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Interception target — this module only depends on OkHttp, not on
    // Retrofit or any app-specific networking setup, so it stays reusable.
    compileOnly(libs.okhttp.core)

    // Compose (self-contained UI: the debugger screens don't reuse the
    // host app's theme, so this module never depends on the app module)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
