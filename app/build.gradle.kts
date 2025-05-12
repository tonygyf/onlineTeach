plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "com.example.onlineteach"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.onlineteach"
        minSdk = 26
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
//添加kts版本lottie依赖
    implementation("com.airbnb.android:lottie:6.1.0")
    
    // 添加Android-Week-View日历库
    implementation("com.github.thellmund:Android-Week-View:5.2.4")
    // ViewPager2用于滑动介绍
    implementation("androidx.viewpager2:viewpager2:1.0.0")
//    kotlin
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

}