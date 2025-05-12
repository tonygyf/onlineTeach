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
        sourceCompatibility = JavaVersion.VERSION_17 // 使用 Java 17
        targetCompatibility = JavaVersion.VERSION_17 // 使用 Java 17
    }

    buildFeatures {
        viewBinding = true
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
    implementation("com.airbnb.android:lottie:6.1.0") // KTS版本lottie依赖
    implementation("com.github.thellmund:Android-Week-View:5.2.4") // 日历库
    implementation("androidx.viewpager2:viewpager2:1.0.0") // ViewPager2依赖
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:2.1.0") // Kotlin依赖
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation ("androidx.concurrent:concurrent-futures:1.1.0")
    implementation ("com.google.guava:guava:33.3.1-android") // 或更新的版本
}

// 更新 JVM 工具链为 Java 17
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // 设置为 Java 17
    }
}
