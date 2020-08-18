import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("kotlin-android-extensions")
}

kotlin {
    android()
    jvm()
    // This is for iPhone emulator
    // Switch here to iosArm64 (or iosArm32) to build library for iPhone device
    iosX64("ios") {
        binaries {
            framework {
                export(project(":lib"))
                transitiveExport = true
            }
        }
    }
    js {
        browser()
        useCommonJs()

        compilations.all {
            kotlinOptions {
                languageVersion = "1.4"
                moduleKind = "commonjs"
                sourceMap = true
                sourceMapEmbedSources = "always"
                metaInfo = true
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":lib"))
                implementation("org.kodein.di:kodein-di:$kodeinVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.compose.runtime:runtime:$compose_version")
                implementation("androidx.compose.ui:ui:$compose_version")
                implementation("androidx.compose.foundation:foundation-layout:$compose_version")
                implementation("androidx.compose.material:material:$compose_version")
                implementation("androidx.compose.material:material-icons-extended:$compose_version")
                implementation("androidx.compose.foundation:foundation:$compose_version")
                implementation("androidx.compose.animation:animation:$compose_version")
                implementation("androidx.ui:ui-tooling:$compose_version")
                implementation("androidx.appcompat:appcompat:1.2.0")
                implementation("androidx.activity:activity-ktx:1.1.0")
                implementation("androidx.core:core-ktx:1.3.1")
                implementation("androidx.constraintlayout:constraintlayout:2.0.0-rc1")
                implementation("com.android.support.constraint:constraint-layout:1.1.3")
                implementation("androidx.recyclerview:recyclerview:1.1.0")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("androidx.ui:ui-test:$compose_version")
                implementation("com.android.support.test:runner:1.0.2")
            }
        }
        val iosMain by getting
        val iosTest by getting
        val jsMain by getting {
            dependencies {
                implementation(project(":js-react"))

                implementation(npm("core-js", "^2.0.0"))
                implementation(Dependencies.React)
                implementation(Dependencies.ReactDom)
                implementation(npm("react", reactVersion))
                implementation(npm("react-dom", reactVersion))
                implementation(npm("react-is", reactVersion))

                val css_version = "1.0.0-pre.91"
                val kotlinext_version = "1.0.1-pre.91"
                implementation("org.jetbrains:kotlin-styled:1.0.0-pre.111-kotlin-1.4.0")
                implementation(npm("styled-components", "^4.3.2"))
                implementation(npm("css-in-js-utils", "^3.0.4"))
                implementation(npm("@jetbrains/kotlin-css", "^$css_version"))
                implementation(npm("@jetbrains/kotlin-css-js", "^$css_version"))
                implementation(npm("@jetbrains/kotlin-extensions", "^$kotlinext_version"))
                implementation(npm("@jetbrains/kotlin-styled", "^$css_version"))
                implementation(npm("inline-style-prefixer", "^5.1.2"))
            }
        }
    }
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "org.jetbrains.kotlin.mpp_app_android"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            minifyEnabled(false)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerVersion = kotlin_version
        kotlinCompilerExtensionVersion = compose_version
    }
}

val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>("ios").binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
}
tasks.getByName("build").dependsOn(packForXcode)
