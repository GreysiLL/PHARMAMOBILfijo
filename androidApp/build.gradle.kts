import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    if (project.file("google-services.json").isFile) {
        repositories { google(); mavenCentral() }
        dependencies {
            classpath("com.google.gms:google-services:4.5.0")
            classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.8")
        }
    }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val firebaseConfigurado = file("google-services.json").isFile
if (firebaseConfigurado) {
    pluginManager.apply("com.google.gms.google-services")
    pluginManager.apply("com.google.firebase.crashlytics")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    if (firebaseConfigurado) {
        implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
        implementation("dev.gitlive:firebase-crashlytics:2.7.0")
        implementation("dev.gitlive:firebase-messaging:2.7.0")
    }
    debugImplementation(libs.sqldelight.runtime)
    debugImplementation(libs.compose.foundation)
    debugImplementation(libs.compose.material3)
    debugImplementation(libs.ktor.client.core)
    debugImplementation(libs.ktor.client.auth)
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    sourceSets.getByName("main").kotlin.srcDir(if (firebaseConfigurado) "src/conFirebase/kotlin" else "src/sinFirebase/kotlin")
    namespace = "pe.edu.upeu.pharmamobil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pe.edu.upeu.pharmamobil"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            if(providers.gradleProperty("aa11Pruebas").orNull == "true") applicationIdSuffix = ".aa11"
        }
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
    buildFeatures {
        compose = true
    }
}
