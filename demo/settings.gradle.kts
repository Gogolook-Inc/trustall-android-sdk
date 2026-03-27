pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/Gogolook-Inc/trustall-android-sdk")
            credentials {
                username = System.getenv("github_actor") ?: extra["github_actor"] as? String ?: ""
                password = System.getenv("github_token") ?: extra["github_token"] as? String ?: ""
            }
        }
        mavenLocal()
    }
}

rootProject.name = "TrustallDemo"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":feature:auth")
include(":feature:search")
include(":feature:offlinedb")
include(":feature:urlscan")
include(":feature:msgfilter")
include(":feature:block")
include(":feature:callerid")
include(":feature:calllog")
include(":feature:smslog")
include(":core:ui")
include(":core:util")
include(":core:designsystem")
