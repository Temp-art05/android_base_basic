plugins {
    id("base.android.library")
}

android {
    namespace = "com.base.app.core.utils"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
