plugins {
    alias(libs.plugins.trustall.android.library)
    alias(libs.plugins.trustall.android.library.compose)
}

android {
    namespace = "com.gogolook.trustall.demo.feature.smslog"

}

dependencies {
    implementation(platform(libs.trustall.bom))
    implementation(libs.trustall.core)
    implementation(libs.trustall.callerid)
    implementation(libs.trustall.contact)
    implementation(libs.trustall.smslog)
    implementation(libs.trustall.msgfilter)
    implementation(libs.trustall.urlscan)
    implementation(libs.trustall.permission)
    implementation(projects.core.ui)
    implementation(projects.core.util)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icon.core)
    implementation(libs.androidx.material.icon.extended)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
}
