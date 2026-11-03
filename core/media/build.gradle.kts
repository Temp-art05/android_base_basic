plugins {
    id("base.android.library")
    id("base.android.compose")
}

android {
    namespace = "com.base.app.core.media"

    // FakeFeedPlayer dùng chung cho test ViewModel của :feature:watch và :feature:main.
    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
    implementation(libs.timber)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui.compose)

    testFixturesImplementation(libs.kotlinx.coroutines.core)
    // Compose compiler cũng chạy trên source set testFixtures — cần runtime trên classpath.
    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.compose.runtime)

    testImplementation(libs.bundles.unit.test)
}
