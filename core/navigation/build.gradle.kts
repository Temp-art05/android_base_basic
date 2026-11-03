plugins {
    id("base.android.library")
    id("base.android.compose")
}

android {
    namespace = "com.base.app.core.navigation"
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.androidx.compose)
    implementation(libs.timber)
}
