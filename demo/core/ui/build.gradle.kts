plugins {
    alias(libs.plugins.trustall.android.library)
    alias(libs.plugins.trustall.android.library.compose)
}

android {
    namespace = "com.gogolook.trustall.demo.core.ui"
}

dependencies {
    implementation(platform(libs.trustall.bom))
    implementation(libs.trustall.callerid)
    implementation(projects.core.designsystem)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
