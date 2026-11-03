pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    // Máy chưa có JDK 17 thì Gradle tự tải về (toolchain khai ở build-logic/ProjectExt.kt).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Lib ads của tác giả (lib-ads-android 1.1.1): bản build AAR + POM vendor vào repo. GitHub Packages luôn cần token,
        // token để trong repo public bị GitHub thu hồi → clone sạch không build được. Nâng lib = chép bản mới vào đây.
        maven {
            url = uri(settingsDir.resolve("third_party/maven"))
            content { includeGroup("com.github.dinhn") }
        }
        // ads-admob kéo theo mediation adapter Pangle + Mintegral (như HomeMorph) — SDK của họ chỉ có ở repo riêng.
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle")
            content { includeGroupByRegex("com\\.pangle.*") }
        }
        maven {
            url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
            content { includeGroupByRegex("com\\.mbridge.*") }
        }
    }
}

rootProject.name = "AndroidBaseBasic"
include(":app")
include(":core:utils")
include(":core:navigation")
include(":core:analytics")
include(":core:ui-foundation")
include(":core:design-system")
include(":core:media")
include(":core:ads")
