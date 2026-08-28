# SMS Flow

`Trustall.smsFlow` (`TrustallSmsFlow`) delivers incoming SMS messages in real time as a Kotlin `Flow`.

The SDK declares the SMS broadcast receiver in its own manifest, so an incoming SMS wakes the host app process even after it has been killed — no receiver registration is required.

## Permission

```xml
<uses-permission android:name="android.permission.RECEIVE_SMS" />
```

The permission is merged into your app's manifest by this module — you only need to request it at runtime via [`requestSmsReceivePermission()`](#requestsmsreceivepermission):

```kotlin
when (Trustall.smsFlow.requestSmsReceivePermission(activity)) {
    is PermissionResult.Granted        -> { /* proceed */ }
    is PermissionResult.ShowRationale  -> { /* explain why the permission is needed, then ask again */ }
    is PermissionResult.NeverAskAgain  -> { /* direct the user to system settings */ }
    is PermissionResult.NotSupported   -> { /* permission not supported on this device */ }
}
```

## Collect Incoming SMS

[`incomingSms`](#incomingsms) is a hot `SharedFlow`. Collect it as early as possible — typically from `Application.onCreate()`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            Trustall.initialize(this@MyApp, config)
            Trustall.smsFlow.incomingSms.collect { sms ->
                Log.d("SmsFlow", "From: ${sms.sender}")
                Log.d("SmsFlow", "Body: ${sms.body}")
            }
        }
    }
}
```

The flow replays up to 16 recent messages of the current process's lifetime to each new collector. This matters for background delivery: when an SMS wakes a killed process, the broadcast is dispatched **before** your `Application`-scoped collector starts — the replay guarantees the collector still observes the message that triggered the wake-up. Replayed messages carry their original `timestampMillis`, so a collector that re-subscribes can de-duplicate if needed.

## Background Delivery

The receiver declared by this module:

- Is guarded with `android.permission.BROADCAST_SMS` — only the system telephony stack holds this permission, so spoofed SMS broadcasts from other apps are rejected.
- Ships with `<uses-feature android:name="android.hardware.telephony" android:required="false" />`, so Google Play does not filter your app off non-telephony devices.

Multipart messages are assembled before emission — `body` always contains the full text.

> **Note:** Android delivers no broadcasts to an app in the *stopped state* (force-stopped from app settings). Normal lifecycle kills — the system reclaiming memory, the user swiping the app away — do not enter this state, and delivery resumes on the next launch after a force stop.

## Scan URLs in Incoming SMS

Combine with [URL Scan](url-scan.md)'s `scanText()` to check every incoming message for malicious links:

```kotlin
Trustall.smsFlow.incomingSms.collect { sms ->
    val results = Trustall.urlScan.scanText(sms.body)
    val hasThreat = results.any {
        it is UrlScanResult.Success &&
            (it.level == Level.MALICIOUS || it.level == Level.SUSPICIOUS)
    }
    if (hasThreat) {
        // warn the user
    }
}
```

---

## API Reference

**Package:** `com.gogolook.trustall.smsflow`

Access via `Trustall.smsFlow`.

### Properties

#### `incomingSms`

```kotlin
val incomingSms: SharedFlow<IncomingSms>
```

Hot flow of incoming SMS. Replays up to 16 recent messages of the current process's lifetime to a new collector, so a collector started from `Application.onCreate()` still observes the message that woke the process.

---

### Functions

#### `hasSmsReceivePermission`

```kotlin
fun hasSmsReceivePermission(): Boolean
```

Returns `true` if `RECEIVE_SMS` is granted.

---

#### `requestSmsReceivePermission`

```kotlin
suspend fun requestSmsReceivePermission(activity: ComponentActivity): PermissionResult
```

Requests `RECEIVE_SMS` permission.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to show the permission dialog |

**Returns:** [`PermissionResult`](#permissionresult)

---

### IncomingSms

| Field | Type | Description |
|-------|------|-------------|
| `sender` | `String` | Originating address (a phone number, or an alphanumeric sender id) |
| `body` | `String` | Full message body, with multipart segments already assembled |
| `timestampMillis` | `Long` | Service-center timestamp (Unix milliseconds) |
| `subscriptionId` | `Int?` | SIM subscription that received the message; `null` when unavailable |

### PermissionResult

| Type | Description |
|------|-------------|
| `Granted` | Permission is granted |
| `ShowRationale` | Should show rationale before asking again |
| `NeverAskAgain` | User denied permanently — direct them to system settings |
| `NotSupported` | Permission not supported on this device |
