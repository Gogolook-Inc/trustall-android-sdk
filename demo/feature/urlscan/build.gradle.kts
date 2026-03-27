plugins {
    alias(libs.plugins.trustall.android.library)
    alias(libs.plugins.trustall.android.library.compose)
}

android {
    namespace = "com.gogolook.trustall.demo.feature.urlscan"
}

dependencies {
    implementation(platform(libs.trustall.bom))
    implementation(libs.trustall.core)
    implementation(libs.trustall.urlscan)
    implementation(projects.core.util)
    
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.lifecycle.viewmodel.compose)
}
