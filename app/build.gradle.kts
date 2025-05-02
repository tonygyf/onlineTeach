plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.onlineteach"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.onlineteach"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
//        添加databinding
        dataBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.room.runtime) // Room 运行时依赖
    annotationProcessor(libs.room.compiler) // Room 编译器依赖（用于生成代码）

    // 添加Lottie依赖 (Kotlin Script 语法)
    implementation("com.airbnb.android:lottie:6.3.0") // 使用双引号包裹依赖字符串
    // 确保也添加了 Splash Screen API 依赖 (Kotlin Script 语法)
    implementation("androidx.core:core-splashscreen:1.0.1") // 使用双引号包裹依赖字符串
}