plugins {
    id("base.android.library")
    id("base.android.compose")
}

android {
    namespace = "com.base.app.core.ads"

    // Test presenter dựng `Activity()`/`ContextWrapper` làm tham số giả — không gọi method nào của chúng.
    testOptions.unitTests.isReturnDefaultValues = true

    // FakeInterstitialAdManager / FakeRewardAdManager / FakeAdsLogToggle dùng chung cho test ViewModel của các feature.
    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:analytics"))
    implementation(project(":core:design-system"))
    implementation(libs.ads.core)
    implementation(libs.ads.admob)
    implementation(libs.ads.firebase)
    // NativeAdView dựng bằng code (NativeAdCardView) — không dùng layout XML của lib.
    implementation(libs.play.services.ads)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)
    implementation(libs.timber)

    testFixturesImplementation(project(":core:utils"))
    testFixturesImplementation(libs.kotlinx.coroutines.core)
    // Compose compiler cũng chạy trên source set testFixtures — cần runtime trên classpath.
    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.compose.runtime)
    testFixturesImplementation(libs.androidx.compose.ui)

    testImplementation(libs.bundles.unit.test)
    testImplementation(project(":core:utils"))
}
