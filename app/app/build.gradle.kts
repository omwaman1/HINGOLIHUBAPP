plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.hingoli.hub"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("../HINGOLIHUBKEY")
            storePassword = "Som@1973"
            keyAlias = "key0"
            keyPassword = "Som@1973"
        }
    }

    defaultConfig {
        applicationId = "com.hingoli.hub"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.0.5"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // NDK ABI filters - only include common architectures
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            buildConfigField("String", "API_BASE_URL", "\"https://hellohingoli.com/apiv5/\"")
        }
        release {
            isMinifyEnabled = true      // Enable code shrinking
            isShrinkResources = true    // Enable resource shrinking
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "API_BASE_URL", "\"https://hellohingoli.com/apiv5/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // ABI Splits - DISABLED for AAB (Play Store handles this automatically)
    // Enable only if building APKs directly
    splits {
        abi {
            isEnable = false  // Play Store AAB auto-splits by architecture
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    // Packaging options to exclude duplicate files
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended) // Needed - ProGuard strips unused
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)
    
    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // DataStore
    implementation(libs.datastore.preferences)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    
    // SMS Retriever API (Auto OTP)
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.2")
    
    // Location Services for GPS coordinates
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // ZegoCloud Voice Call - uses dynamic versioning as recommended by ZegoCloud docs
    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    
    // Razorpay Payment Gateway
    implementation("com.razorpay:checkout:1.6.33")
    
    // Baseline Profiles - 30-40% faster cold start
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
}
