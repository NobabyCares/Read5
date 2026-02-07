plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.read5"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.read5"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // 可选：启用 Kotlin 编译器优化
        freeCompilerArgs += listOf(
            "-Xcontext-receivers",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

// ✅ 官方 foundation（包含 zoomable）
    implementation(libs.androidx.compose.foundation)

    //    pdf 库
    // PDF 查看器 —— 使用正确的第三方库
//    implementation("com.github.barteksc:AndroidPdfViewer:3.1.0-beta.1")
   /* implementation("com.github.barteksc:AndroidPdfViewer:3.1.0-beta.1") {
        exclude(group = "com.android.support")
    }*/

    //
    /*👇 关键：添加 foundation（自动匹配 BOM 版本）, 构建 UI 所需的基础交互、布局原语和实用组件。
    * 我这里主要是交互逻辑
    *
    * */
    implementation("androidx.compose.foundation:foundation")


    // PDF 查看器
    implementation("com.github.TalbotGooday:AndroidPdfViewer:3.1.0-beta.3"){
        exclude(group = "com.android.support")
    }


/*    implementation("com.github.barteksc:pdfium-android:1.9.0"){
        exclude(group = "com.android.support")
    }*/

    // 👇 添加这一行 SAF 遍历
    implementation(libs.androidx.documentfile)

    implementation(libs.coil.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.storage)

    kapt(libs.androidx.room.compiler) // 或 annotationProcessor


    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // AndroidX & Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Hilt
    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.documentfile)
    androidTestImplementation(libs.androidx.arch.core.testing)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.arch.core.testing) // 提供 InstantTaskExecutorRule

    // Android Instrumented Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Kapt configuration
kapt {
    correctErrorTypes = true
    // 可选：提升注解处理性能
    useBuildCache = true
}

