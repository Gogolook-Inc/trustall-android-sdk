plugins {
    alias(libs.plugins.trustall.android.library)
    alias(libs.plugins.trustall.android.library.compose)
}

android {
    namespace = "com.gogolook.trustall.demo.core.designsystem"
}

dependencies {
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.ktx)
}
