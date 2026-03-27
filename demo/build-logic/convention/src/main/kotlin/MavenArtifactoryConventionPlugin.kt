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
import com.gogolook.trustall.PublicationDataExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra

class MavenArtifactoryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
            }

            val publicationData =
                extensions.create("publicationData", PublicationDataExtension::class.java)
            val packagingName = if (publicationData.bom) "bom" else "aar"

            val publishGroupId = { group.toString() }
            val publishArtifactId = { name }
            val publishVersion = { version.toString() }
            val buildFilePath =
                { "${project.layout.buildDirectory.get().asFile.path}/outputs/aar/${publishArtifactId()}-${publishVersion()}.aar" }

            afterEvaluate {
                configure<LibraryExtension> {
                    libraryVariants.all {
                        outputs.all {
                            outputFile.renameTo(project.file(buildFilePath()))
                        }
                    }
                }

                configure<PublishingExtension> {

                    repositories {
                        maven {
                            name = rootProject.extra["uploadMavenRepo"] as String
                            url = uri(rootProject.extra["uploadMavenUrl"] as String)
                            credentials {
                                username = rootProject.extra["uploadMavenUsername"] as String
                                password = rootProject.extra["uploadMavenPassword"] as String
                            }
                        }
                    }

                    publications {
                        create(packagingName, MavenPublication::class.java) {
                            groupId = publishGroupId()
                            artifactId = publishArtifactId()
                            version = publishVersion()
                            if (publicationData.bom.not()) {
                                artifact(buildFilePath())
                            }

                            pom.withXml {
                                if (publicationData.bom) {
                                    asNode()
                                        .appendNode("dependencyManagement")
                                        .appendNode("dependencies")
                                        .also { dependenciesNode ->
                                            configurations.findByName("implementation")
                                                ?.allDependencies
                                                ?.filter {
                                                    it.group != null
                                                }
                                                ?.forEach {
                                                    dependenciesNode.appendNode("dependency")
                                                        .apply {
                                                            appendNode("groupId", it.group)
                                                            appendNode("artifactId", it.name)
                                                            appendNode("version", it.version)
                                                        }
                                                }
                                        }
                                } else {
                                    if (containBomLib(configurations)) {
                                        asNode()
                                            .appendNode("dependencyManagement")
                                            .appendNode("dependencies")
                                            .also { dependenciesNode ->
                                                configurations.findByName("implementation")
                                                    ?.allDependencies
                                                    ?.filter {
                                                        it.group != null
                                                                && it.name.contains(BOM_LIB_PATTERN)
                                                    }
                                                    ?.forEach {
                                                        dependenciesNode.appendNode("dependency")
                                                            .apply {
                                                                appendNode("groupId", it.group)
                                                                appendNode("artifactId", it.name)
                                                                appendNode("version", it.version)
                                                                appendNode("type", "pom")
                                                                appendNode("scope", "import")
                                                            }
                                                    }
                                            }
                                    }

                                    val dependenciesNode = asNode().appendNode("dependencies")
                                    configurations.findByName("implementation")?.allDependencies
                                        ?.filter {
                                            it.group != null
                                                    && it.name.contains(BOM_LIB_PATTERN).not()
                                        }
                                        ?.forEach {
                                            dependenciesNode.appendNode("dependency").apply {
                                                appendNode("groupId", it.group)
                                                appendNode("artifactId", it.name)
                                                appendNode("scope", "runtime")
                                                if (it.version != null) {
                                                    appendNode("version", it.version)
                                                } else {
                                                    appendNode("version")
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun containBomLib(configurations: ConfigurationContainer): Boolean {
        return configurations.findByName("implementation")?.allDependencies
            ?.any { it.group != null && it.name.contains(BOM_LIB_PATTERN) } ?: false
    }

    companion object {
        const val BOM_LIB_PATTERN = "-bom"
    }
}
