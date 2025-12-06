import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    kotlin("native.cocoapods")
}

kotlin {
    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
    
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        iosTarget.compilations.getByName("main") {
            cinterops {
                val LiveKitWrappers by creating {
                    defFile(project.file("src/nativeInterop/cinterop/LiveKitWrappers.def"))
                    packageName("com.justcalls.livekit.wrappers")
                    compilerOpts("-framework", "Foundation", "-framework", "UIKit")
                }
            }
        }
    }
    
    cocoapods {
        summary = "JustCalls iOS App"
        homepage = "https://github.com/justcalls"
        version = "1.0"
        ios.deploymentTarget = "15.0"
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        // LiveKit будет добавлен через Swift Package Manager в Xcode проекте
        // pod("LiveKit", "~> 2.0")
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            // LiveKit SDK для Android
            implementation("io.livekit:livekit-android:2.4.0")
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)

        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.coil.compose)
            implementation("io.coil-kt.coil3:coil-svg:3.2.0")
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation("io.ktor:ktor-client-core:3.1.3")
        }
    }
}

android {
    namespace = "com.justcalls"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("io.coil-kt.coil3:coil-svg:3.0.4")
}
