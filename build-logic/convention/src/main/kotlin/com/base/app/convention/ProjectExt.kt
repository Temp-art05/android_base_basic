package com.base.app.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Giá trị dùng chung cho mọi module Android. Đổi ở đây là đổi toàn project —
 * không còn copy-paste compileSdk/minSdk/Java version vào từng build file.
 */
object AndroidConfig {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val MIN_SDK = 24
    val JAVA_VERSION = JavaVersion.VERSION_17
    const val JVM_TOOLCHAIN = 17
}

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Cấu hình chung cho mọi module Android (application lẫn library).
 */
internal fun Project.configureAndroidCommon(extension: CommonExtension) {
    extension.apply {
        compileSdk = AndroidConfig.COMPILE_SDK

        defaultConfig.apply {
            minSdk = AndroidConfig.MIN_SDK
        }

        compileOptions.apply {
            sourceCompatibility = AndroidConfig.JAVA_VERSION
            targetCompatibility = AndroidConfig.JAVA_VERSION
        }

        // Rule CLAUDE.md: thêm string là dịch đủ 10 locale — thiếu bản dịch làm đỏ lint.
        lint.apply {
            error += "MissingTranslation"
            checkDependencies = true
        }
    }

    configureKotlinToolchain()
}

/**
 * Ghim JDK dùng để compile Kotlin, để build không phụ thuộc JAVA_HOME của máy dev/CI.
 * AGP 9 dùng built-in Kotlin nên `kotlin {}` có sẵn mà không cần plugin kotlin-android.
 */
internal fun Project.configureKotlinToolchain() {
    extensions.configure(org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension::class.java) {
        jvmToolchain(AndroidConfig.JVM_TOOLCHAIN)
    }
}
