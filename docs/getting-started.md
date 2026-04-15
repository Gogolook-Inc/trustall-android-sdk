# Getting Started — Android

## Requirements

- minSdk **29+**
- Kotlin **1.9+**

## Installation

### 1. Add the Maven Repository

The SDK is hosted on GitHub Packages. Add the repository to your `settings.gradle` (or project-level `build.gradle`):

> **Note:** The package is public. Any GitHub personal access token (classic or fine-grained) with the **read:packages** scope will work — it does not need to belong to a specific organization.

#### Kotlin DSL

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/Gogolook-Inc/trustall-android-sdk")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: extra["github_actor"] as? String ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: extra["github_token"] as? String ?: ""
            }
        }
    }
}
```

#### Groovy

```groovy
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri('https://maven.pkg.github.com/Gogolook-Inc/trustall-android-sdk')
            credentials {
                username = System.getenv('GITHUB_ACTOR') ?: (extra.has('github_actor') ? extra['github_actor'] : '')
                password = System.getenv('GITHUB_TOKEN') ?: (extra.has('github_token') ? extra['github_token'] : '')
            }
        }
    }
}
```

Store your credentials in `~/.gradle/gradle.properties` (never commit these):

```properties
github_actor=YOUR_GITHUB_USERNAME
github_token=YOUR_GITHUB_TOKEN
```

### 2. Add Dependencies

Import the BOM, then add feature modules without specifying versions:

#### Kotlin DSL

```kotlin
dependencies {
    // Import the BOM
    implementation(platform("com.gogolook.trustall:trustall-bom:2026.04.01"))

    // Core (required)
    implementation("com.gogolook.trustall:trustall-core")

    // Add feature modules as needed — no version required
    implementation("com.gogolook.trustall:trustall-auth")
    implementation("com.gogolook.trustall:trustall-callerid")
    implementation("com.gogolook.trustall:trustall-calllog")
    implementation("com.gogolook.trustall:trustall-contact")
    implementation("com.gogolook.trustall:trustall-msgfilter")
    implementation("com.gogolook.trustall:trustall-numberblock")
    implementation("com.gogolook.trustall:trustall-numbersearch")
    implementation("com.gogolook.trustall:trustall-offlinedb")
    implementation("com.gogolook.trustall:trustall-smslog")
    implementation("com.gogolook.trustall:trustall-urlscan")

    // Network environment (pick one)
    implementation("com.gogolook.trustall.network:production")
    // implementation("com.gogolook.trustall.network:staging")
    // implementation("com.gogolook.trustall.network:sandbox")
}
```

#### Groovy

```groovy
dependencies {
    // Import the BOM
    implementation platform('com.gogolook.trustall:trustall-bom:2026.04.01')

    // Core (required)
    implementation 'com.gogolook.trustall:trustall-core'

    // Add feature modules as needed — no version required
    implementation 'com.gogolook.trustall:trustall-auth'
    implementation 'com.gogolook.trustall:trustall-callerid'
    implementation 'com.gogolook.trustall:trustall-calllog'
    implementation 'com.gogolook.trustall:trustall-contact'
    implementation 'com.gogolook.trustall:trustall-msgfilter'
    implementation 'com.gogolook.trustall:trustall-numberblock'
    implementation 'com.gogolook.trustall:trustall-numbersearch'
    implementation 'com.gogolook.trustall:trustall-offlinedb'
    implementation 'com.gogolook.trustall:trustall-smslog'
    implementation 'com.gogolook.trustall:trustall-urlscan'

    // Network environment (pick one)
    implementation 'com.gogolook.trustall.network:production'
    // implementation 'com.gogolook.trustall.network:staging'
    // implementation 'com.gogolook.trustall.network:sandbox'
}
```

## Initialization

Call `Trustall.initialize()` in `Application.onCreate()` with a `SdkConfig`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        coroutineScope.launch {
            Trustall.initialize(
                app = this@MyApp,
                config = SdkConfig(
                    licenseId = "YOUR_LICENSE_ID",
                    isDebug = BuildConfig.DEBUG,
                )
            )
        }
    }
}
```

Register `MyApp` in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApp"
    ...>
```

## SdkConfig

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `licenseId` | `String` | — | License ID provided by Trustall |
| `isDebug` | `Boolean` | `false` | Enable debug logging |

## Observing Initialization State

`Trustall.isInitialized` is a `StateFlow<Boolean>` you can collect to know when the SDK is ready:

```kotlin
lifecycleScope.launch {
    Trustall.isInitialized.collect { ready ->
        if (ready) {
            // SDK is ready — call any module
        }
    }
}
```

> **Tip:** We recommend holding the splash screen until `isInitialized` emits `true`. This ensures the SDK is fully ready before the user reaches your main UI.
>
> ```kotlin
> class MainActivity : AppCompatActivity() {
>     override fun onCreate(savedInstanceState: Bundle?) {
>         val splashScreen = installSplashScreen()
>
>         splashScreen.setKeepOnScreenCondition {
>             !Trustall.isInitialized.value
>         }
>
>         super.onCreate(savedInstanceState)
>         // ...
>     }
> }
> ```

## SDK Version & Device ID

```kotlin
val version  = Trustall.sdkVersion  // e.g. "2026.04.01"
val deviceId = Trustall.deviceId    // Device identifier generated by the SDK
```

## Feature Modules

| Property | Module | Description |
|----------|--------|-------------|
| `Trustall.auth` | trustall-auth | Device and member registration |
| `Trustall.callerId` | trustall-callerid | Caller ID and call events |
| `Trustall.callLog` | trustall-calllog | Call log retrieval and upload |
| `Trustall.contact` | trustall-contact | Contact lookup |
| `Trustall.messageFilter` | trustall-msgfilter | Message classification |
| `Trustall.numberBlock` | trustall-numberblock | Number blocking |
| `Trustall.numberSearch` | trustall-numbersearch | Online number lookup |
| `Trustall.offlineDb` | trustall-offlinedb | Offline number database |
| `Trustall.smsLog` | trustall-smslog | SMS / MMS log retrieval |
| `Trustall.urlScan` | trustall-urlscan | URL threat scanning |
