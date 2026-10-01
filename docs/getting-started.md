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
    implementation(platform("com.gogolook.trustall:trustall-bom:2026.09.01"))

    // Core (required)
    implementation("com.gogolook.trustall:trustall-core")

    // Add feature modules as needed — no version required
    implementation("com.gogolook.trustall:trustall-callerid")
    implementation("com.gogolook.trustall:trustall-calllog")
    implementation("com.gogolook.trustall:trustall-contact")
    implementation("com.gogolook.trustall:trustall-msgfilter")
    implementation("com.gogolook.trustall:trustall-numberblock")
    implementation("com.gogolook.trustall:trustall-numbersearch")
    implementation("com.gogolook.trustall:trustall-offlinedb")
    implementation("com.gogolook.trustall:trustall-smsflow")
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
    implementation platform('com.gogolook.trustall:trustall-bom:2026.09.01')

    // Core (required)
    implementation 'com.gogolook.trustall:trustall-core'

    // Add feature modules as needed — no version required
    implementation 'com.gogolook.trustall:trustall-callerid'
    implementation 'com.gogolook.trustall:trustall-calllog'
    implementation 'com.gogolook.trustall:trustall-contact'
    implementation 'com.gogolook.trustall:trustall-msgfilter'
    implementation 'com.gogolook.trustall:trustall-numberblock'
    implementation 'com.gogolook.trustall:trustall-numbersearch'
    implementation 'com.gogolook.trustall:trustall-offlinedb'
    implementation 'com.gogolook.trustall:trustall-smsflow'
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
| `deviceId` | `(suspend () -> String)?` | `null` | Supplies your own device ID — see [Device ID](#device-id) |

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
val version  = Trustall.sdkVersion  // e.g. "2026.09.01"
val deviceId = Trustall.deviceId    // Device identifier in use — see below
```

## Device ID

The device ID identifies this installation to the Trustall backend. By default the SDK generates a random UUID on first run and keeps it in local storage, so it stays the same across launches until the app's data is cleared.

To use your own identifier, pass `deviceId` in `SdkConfig`. The lambda is `suspend`, so the value can come from your storage or backend, and it is invoked once per `initialize()`:

```kotlin
Trustall.initialize(
    app = this@MyApp,
    config = SdkConfig(
        licenseId = "YOUR_LICENSE_ID",
        deviceId = { myBackend.fetchDeviceId() },
    )
)
```

- The value must be exactly 32 lowercase hex characters (a UUID with the dashes removed). Anything else makes `initialize()` throw `IllegalArgumentException`.
- The value is stored in place of the previous one. A later `initialize()` that leaves `deviceId` as `null` keeps using it.
- The device ID changes when the app's data is cleared (a new random one is generated) or when a later `initialize()` supplies a different value. Either way the device registers again, and the previous registration's member ID is discarded.
- `initialize()` can be called again on a running SDK to adopt a new device ID without a relaunch. Other settings such as `isDebug` still require a relaunch.

## Custom Providers

Four modules let you replace the backend they talk to with your own implementation:

| Module | Interface | Register with |
|--------|-----------|---------------|
| [Number Search](number-search.md#custom-providers) | `NumberSearchProvider` | `TrustallNumberSearch.setProviders(list)` |
| [URL Scan](url-scan.md#custom-providers) | `UrlScanProvider` | `TrustallUrlScan.setProviders(list)` |
| [Offline DB](offline-db.md#custom-provider) | `OfflineDbProvider` | `TrustallOfflineDb.setProvider(provider)` |
| [Call Log](call-log.md#custom-upload-destination) | `CallLogUploadProvider` | `TrustallCallLog.setProvider(provider)` |

Register providers before `Trustall.initialize()` so that the first request already goes to your implementation. Each feature page describes how its providers are consulted and which other modules are affected.

Rules common to every provider:

- The interfaces use `suspend` functions, so they can only be implemented in Kotlin.
- Provider functions run on the caller's dispatcher. Move blocking I/O to your own dispatcher.
- The SDK applies no timeout of its own. Wrap slow calls in `withTimeout()`; an expired timeout is treated as that provider failing, never as a cancellation of the caller.
- Every module exposes its built-in Gogolook implementation as `defaultProvider`, so you can keep it as a fallback where the module accepts a list.
- Declare the module whose interface you implement in your own dependencies. `trustall-numbersearch` and `trustall-offlinedb` do not reach your compile classpath through `trustall-callerid`.

## Feature Modules

| Property | Module | Description |
|----------|--------|-------------|
| `Trustall.auth` | trustall-core | Device and member registration |
| `Trustall.callerId` | trustall-callerid | Caller ID and call events |
| `Trustall.callLog` | trustall-calllog | Call log retrieval and upload |
| `Trustall.contact` | trustall-contact | Contact lookup |
| `Trustall.messageFilter` | trustall-msgfilter | Message classification |
| `Trustall.numberBlock` | trustall-numberblock | Number blocking |
| `Trustall.numberSearch` | trustall-numbersearch | Online number lookup |
| `Trustall.offlineDb` | trustall-offlinedb | Offline number database |
| `Trustall.smsFlow` | trustall-smsflow | Real-time incoming SMS events |
| `Trustall.smsLog` | trustall-smslog | SMS / MMS log retrieval |
| `Trustall.urlScan` | trustall-urlscan | URL threat scanning |
