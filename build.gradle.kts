// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) version "2.1.0" apply false
    id("androidx.navigation.safeargs") version "2.7.7" apply false
}

// 添加jitpack仓库支持
