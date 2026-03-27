import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.gogolook.trustall.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    implementation(libs.truth)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    // register the convention plugin
    plugins {
        register("androidLibraryCompose") {
            id = libs.plugins.trustall.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.trustall.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryJacoco") {
            id = libs.plugins.trustall.android.library.jacoco.get().pluginId
            implementationClass = "AndroidLibraryJacocoConventionPlugin"
        }
        register("androidTest") {
            id = libs.plugins.trustall.android.test.get().pluginId
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("androidRoom") {
            id = libs.plugins.trustall.android.room.get().pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidLint") {
            id = libs.plugins.trustall.android.lint.get().pluginId
            implementationClass = "AndroidLintConventionPlugin"
        }
        register("mavenArtifactory") {
            id = libs.plugins.trustall.maven.artifactory.get().pluginId
            implementationClass = "MavenArtifactoryConventionPlugin"
        }
    }
}
