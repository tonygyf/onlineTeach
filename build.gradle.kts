// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false // ✅ 更推荐使用 alias 而非直接写 version
}

// 添加jitpack仓库支持
