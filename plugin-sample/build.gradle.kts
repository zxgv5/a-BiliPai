plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bilipai.plugins.sample"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
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
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // 编译时依赖（不打包进 DEX）
    compileOnly(project(":app"))
    
    // Compose
    compileOnly(platform("androidx.compose:compose-bom:2024.02.00"))
    compileOnly("androidx.compose.ui:ui")
    compileOnly("androidx.compose.material3:material3")
    compileOnly("androidx.compose.runtime:runtime")
}
