/*
 * Copyright 2022 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.android.build.gradle.LibraryExtension
import com.gogolook.trustall.configureKotlinAndroid
import com.gogolook.trustall.libs
import com.gogolook.trustall.targetSdk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.targetSdk
                testOptions.animationsDisabled = true
                if (target.name.startsWith("trustall-")) {
                    buildTypes {
                        getByName("release") {
                            isMinifyEnabled = true
                            
                            // Generate a unique proguard rule for each module to prevent class name collisions (e.g. a.a)
                            val buildDir = target.layout.buildDirectory.get().asFile
                            val uniqueProguardRules = java.io.File(buildDir, "generated/proguard/unique_package_rules.pro")
                            uniqueProguardRules.parentFile.mkdirs()
                            // com.gogolook.trustall.internal.{module_name}
                            val uniquePackage = "com.gogolook.trustall.internal." + target.name.replace("-", ".")
                            uniqueProguardRules.writeText("-repackageclasses $uniquePackage")

                            proguardFiles(
                                getDefaultProguardFile("proguard-android-optimize.txt"),
                                target.rootProject.file("proguard/trustall-sdk.pro"),
                                uniqueProguardRules
                            )
                        }
                    }
                }
//                configureFlavors(this)
                // The resource prefix is derived from the module name,
                // so resources inside ":core:module1" must be prefixed with "core_module1_"
                resourcePrefix = path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = "_").lowercase() + "_"
            }
        }
    }
}
