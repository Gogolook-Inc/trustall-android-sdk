# trustall-android-sdk

Trustall Android SDK — 為 Android 應用提供來電顯示、垃圾電話辨識、訊息過濾、URL 掃描等功能的模組化 SDK。

## Requirements

- minSdk **29+**
- Kotlin **1.9+**

## Quick Start

### 1. Add the Maven Repository

The SDK is hosted on GitHub Packages. Add the following to your `settings.gradle.kts`:

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

Store credentials in `~/.gradle/gradle.properties` (never commit these):

```properties
github_actor=YOUR_GITHUB_USERNAME
github_token=YOUR_GITHUB_TOKEN
```

> **Note:** The package is public. Any GitHub personal access token with the **read:packages** scope will work.

### 2. Add Dependencies

```kotlin
dependencies {
    // Import the BOM
    implementation(platform("com.gogolook.trustall:trustall-bom:2026.08.01"))

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
    implementation("com.gogolook.trustall:trustall-smsflow")
    implementation("com.gogolook.trustall:trustall-smslog")
    implementation("com.gogolook.trustall:trustall-urlscan")

    // Network environment (pick one)
    implementation("com.gogolook.trustall.network:production")
    // implementation("com.gogolook.trustall.network:staging")
    // implementation("com.gogolook.trustall.network:sandbox")
}
```

### 3. Initialize

Call `Trustall.initialize()` in `Application.onCreate()`:

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

## Feature Modules

| Property | Module | Docs |
|----------|--------|------|
| `Trustall.auth` | `trustall-auth` | [Auth](docs/auth.md) |
| `Trustall.callerId` | `trustall-callerid` | [Caller ID](docs/caller-id.md) |
| `Trustall.callLog` | `trustall-calllog` | [Call Log](docs/call-log.md) |
| `Trustall.contact` | `trustall-contact` | [Contact](docs/contact.md) |
| `Trustall.messageFilter` | `trustall-msgfilter` | [Message Filter](docs/message-filter.md) |
| `Trustall.numberBlock` | `trustall-numberblock` | [Number Block](docs/number-block.md) |
| `Trustall.numberSearch` | `trustall-numbersearch` | [Number Search](docs/number-search.md) |
| `Trustall.offlineDb` | `trustall-offlinedb` | [Offline DB](docs/offline-db.md) |
| `Trustall.smsFlow` | `trustall-smsflow` | [SMS Flow](docs/sms-flow.md) |
| `Trustall.smsLog` | `trustall-smslog` | [SMS Log](docs/sms-log.md) |
| `Trustall.urlScan` | `trustall-urlscan` | [URL Scan](docs/url-scan.md) |

## Documentation

- [Getting Started](docs/getting-started.md) — Installation, initialization, and SDK overview
- [Auth](docs/auth.md) — Device registration and member ID management
- [Caller ID](docs/caller-id.md) — Call event callbacks and number info lookup
- [Call Log](docs/call-log.md) — Call log retrieval and upload
- [Contact](docs/contact.md) — Contact lookup by phone number
- [Message Filter](docs/message-filter.md) — Message classification (spam, promotion, transaction)
- [Number Block](docs/number-block.md) — Local block list management
- [Number Search](docs/number-search.md) — Online number lookup with cache
- [Offline DB](docs/offline-db.md) — On-device number database
- [SMS Flow](docs/sms-flow.md) — Real-time incoming SMS events with background delivery
- [SMS Log](docs/sms-log.md) — SMS and MMS log retrieval
- [URL Scan](docs/url-scan.md) — URL threat scanning
- [Number Categories](docs/number-categories.md) — `bizCategory` and `spamCategory` reference
- [Release Notes](docs/changelog.md) — Version history and breaking changes

## Sample App

A fully working demo app is available in the [`demo/`](demo/) directory.
