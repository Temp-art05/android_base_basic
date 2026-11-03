import com.android.build.api.dsl.ProductFlavor
import java.util.Properties

plugins {
    id("base.android.application")
    id("base.android.compose")
    // Destination của ScreenProvider là @Serializable — thiếu plugin này app crash lúc dựng NavHost.
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

// Firebase thật: chỉ cần thả app/google-services.json vào, 2 plugin tự bật. Không có file thì app vẫn chạy
// (Analytics/Crash/RemoteConfig no-op — xem :core:analytics).
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

fun localProperty(key: String): String? = localProperties.getProperty(key)?.takeIf { it.isNotBlank() }

android {
    namespace = "com.base.app"

    defaultConfig {
        applicationId = "com.base.app"
        versionCode = 1
        versionName = "1.0.0"
    }

    androidResources {
        // Chỉ đóng gói locale thực sự hỗ trợ (thay cho resourceConfigurations đã deprecated)
        // Qualifier thư mục res (Indonesia = mã cũ `in`); khớp AppLocale + locales_config.xml.
        localeFilters += listOf("en", "hi", "ta", "in", "ar", "fr", "es", "pt-rBR", "tr", "de")
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    // Keystore + mật khẩu đọc từ local.properties (không commit). Thiếu thì release ra APK unsigned.
    val releaseStoreFile = localProperty("RELEASE_STORE_FILE")?.let(rootProject::file)?.takeIf { it.exists() }
    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = localProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            isDefault = true
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("boolean", "IS_DEV_FLAVOR", "true")
            stringBuildConfig("BASE_URL", "https://dummyjson.com/")
        }

        create("prod") {
            dimension = "environment"
            buildConfigField("boolean", "IS_DEV_FLAVOR", "false")
            stringBuildConfig("BASE_URL", "https://dummyjson.com/")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            buildConfigField("boolean", "IS_DEBUG", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfigs.findByName("release")?.let { signingConfig = it }
            bundle {
                language {
                    // Giữ mọi ngôn ngữ trong bundle để in-app language picker đổi được runtime
                    enableSplit = false
                }
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEBUG", "false")
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:navigation"))
    implementation(project(":core:analytics"))
    implementation(project(":core:ui-foundation"))
    implementation(project(":core:design-system"))
    implementation(project(":core:media"))
    implementation(project(":core:ads"))

    // presentation
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.timber)

    // data
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(testFixtures(project(":core:media"))) // FakeFeedPlayer cho test Reels/Player
}

fun ProductFlavor.stringBuildConfig(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
}
