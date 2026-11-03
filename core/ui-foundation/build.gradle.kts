plugins {
    id("base.android.library")
    id("base.android.compose")
}

android {
    namespace = "com.base.app.core.ui.foundation"

    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(project(":core:utils"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
