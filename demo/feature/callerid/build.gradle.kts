plugins {
    id("trustall.android.library")
    id("trustall.android.library.compose")
}

android {
    namespace = "com.gogolook.trustall.demo.feature.callerid"
}

dependencies {
    implementation(platform(libs.trustall.bom))
    implementation(libs.trustall.core)
    implementation(libs.trustall.callerid)
    implementation(libs.trustall.contact)
    implementation(libs.trustall.permission)
    implementation(libs.trustall.numberblock)
    implementation(libs.trustall.numbersearch)
    implementation(projects.core.ui)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
