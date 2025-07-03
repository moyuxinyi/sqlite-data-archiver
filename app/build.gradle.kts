plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // ksp 是 Kotlin 的注解处理工具，用于处理 Kotlin 的注解处理器。
    // 插件文档: https://developer.android.com/build/migrate-to-ksp?hl=zh-cn#add-ksp
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.xinyi.dbarchiver"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xinyi.dbarchiver"
        minSdk = 21
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // room, SQLite 数据库的封装库
    // 开源地址: https://github.com/androidx/androidx/tree/androidx-main/room
    // 官方文档: https://developer.android.com/jetpack/androidx/releases/room
    implementation(libs.androidx.room.runtime)
    // room 的 Kotlin 扩展库，提供了更多便捷的 Kotlin 扩展函数。
    implementation(libs.androidx.room.ktx)
    // room 的注解处理器，用于在编译时生成代码，简化数据库的操作。
    ksp(libs.androidx.room.compiler)

    // gson, JSON 解析库
    // 开源地址: https://github.com/google/gson
    implementation(libs.gson)
}