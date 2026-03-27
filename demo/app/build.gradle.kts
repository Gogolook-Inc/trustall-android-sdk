plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.gogolook.trustall.demo.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gogolook.trustalldemo"

        minSdk = 29

        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val licenseId = System.getenv("LICENSE_ID") ?: extra["license_id"] ?: ""
        buildConfigField("String", "LICENSE_ID", "\"$licenseId\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
    
    kotlinOptions {
        jvmTarget = "19"
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(projects.feature.auth)
    implementation(projects.feature.search)
    implementation(projects.feature.offlinedb)
    implementation(projects.feature.urlscan)
    implementation(projects.feature.msgfilter)
    implementation(projects.feature.block)
    implementation(projects.feature.callerid)
    implementation(projects.feature.calllog)
    implementation(projects.feature.smslog)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.util)

    implementation(platform(libs.trustall.bom))
    implementation(libs.trustall.core)
    implementation(libs.trustall.network.production)
    implementation(libs.trustall.auth)
    implementation(libs.trustall.callerid)
    implementation(libs.trustall.numberblock)
    implementation(libs.trustall.numbersearch)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icon.core)
    implementation(libs.androidx.material.icon.extended)
    implementation(libs.androidx.core.splashscreen)
}
