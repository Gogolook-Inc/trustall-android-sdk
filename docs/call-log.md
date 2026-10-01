# Call Log

`Trustall.callLog` (`TrustallCallLog`) provides call log retrieval and upload.

## Permission

```xml
<uses-permission android:name="android.permission.READ_CALL_LOG" />
```

Call [`requestCallLogPermission()`](#requestcalllogpermission) to request the permission at runtime:

```kotlin
when (Trustall.callLog.requestCallLogPermission(activity)) {
    is PermissionResult.Granted        -> { /* proceed */ }
    is PermissionResult.ShowRationale  -> { /* explain why the permission is needed, then ask again */ }
    is PermissionResult.NeverAskAgain  -> { /* direct the user to system settings */ }
    is PermissionResult.NotSupported   -> { /* permission not supported on this device */ }
}
```

## Retrieve Call Logs

[`getCallLogs()`](#getcalllogs) accepts an optional time range (millisecond timestamps):

```kotlin
// Get all call logs
val logs = Trustall.callLog.getCallLogs()

// Get logs within a time range
val logs = Trustall.callLog.getCallLogs(
    startTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
    endTime   = System.currentTimeMillis(),
)

logs.forEach { log ->
    Log.d("CallLog", "${log.number} | ${log.type} | ${log.duration}s")
}
```

## Upload Call Logs

```kotlin
// Upload a specific list
val result = Trustall.callLog.uploadCallLogs(logs)

// Auto-upload: uploads all logs since the last uploaded timestamp
val result = Trustall.callLog.autoUploadCallLogs()

when (result) {
    is UploadResult.Success      -> Log.d("CallLog", "Upload success")
    is UploadResult.Error        -> Log.e("CallLog", "Error ${result.code}: ${result.message}")
    is UploadResult.NetworkError -> Log.e("CallLog", "Network error", result.exception)
}
```

## Check Permission

```kotlin
val hasPermission = Trustall.callLog.hasCallLogPermission()
```

## Custom Upload Destination

Implement [`CallLogUploadProvider`](#callloguploadprovider) to send uploaded call logs to your own backend, and register it with [`setProvider()`](#setprovider) before `Trustall.initialize()`:

```kotlin
class MyUploader : CallLogUploadProvider {
    override suspend fun uploadCallLogs(callLogs: List<CallLog>): UploadResult =
        if (myBackend.send(callLogs)) UploadResult.Success
        else UploadResult.Error(500, "upload rejected")
}

TrustallCallLog.setProvider(MyUploader())
```

- A single destination replaces Gogolook's. `setProvider(null)` restores it.
- `uploadCallLogs()` passes its list straight through. `autoUploadCallLogs()` sends batches of 20, oldest first, and advances its watermark after each batch that returns `Success`; any other result stops the run there, and those logs are retried next time. The watermark is kept when the provider changes, so a provider registered later receives only calls since the last successful upload.
- Report failures as `UploadResult.Error` or `UploadResult.NetworkError`. A throw is reported as `NetworkError`.
- `getCallLogs()` and the permission helpers read the device only and are unaffected.

See [Custom Providers](getting-started.md#custom-providers) for the rules common to every provider.

---

## API Reference

**Package:** `com.gogolook.trustall.calllog`

Access via `Trustall.callLog`.

### Functions

#### `getCallLogs`

```kotlin
suspend fun getCallLogs(startTime: Long? = null, endTime: Long? = null): List<CallLog>
```

Returns call logs within an optional time range.

| Parameter | Type | Description |
|-----------|------|-------------|
| `startTime` | `Long?` | Start time in Unix milliseconds (inclusive). Omit to start from the beginning. |
| `endTime` | `Long?` | End time in Unix milliseconds (inclusive). Omit to include up to the latest. |

**Returns:** `List<`[`CallLog`](#calllog)`>`

---

#### `uploadCallLogs`

```kotlin
suspend fun uploadCallLogs(callLogs: List<CallLog>): UploadResult
```

Uploads the given call logs to the backend.

| Parameter | Type | Description |
|-----------|------|-------------|
| `callLogs` | `List<`[`CallLog`](#calllog)`>` | Call logs to upload |

**Returns:** [`UploadResult`](#uploadresult)

---

#### `autoUploadCallLogs`

```kotlin
suspend fun autoUploadCallLogs(): UploadResult
```

Uploads all call logs since the last uploaded timestamp. The watermark is persisted across calls so only new entries are uploaded each time.

**Returns:** [`UploadResult`](#uploadresult)

---

#### `hasCallLogPermission`

```kotlin
fun hasCallLogPermission(): Boolean
```

Returns `true` if `READ_CALL_LOG` is granted.

---

#### `requestCallLogPermission`

```kotlin
suspend fun requestCallLogPermission(activity: ComponentActivity): PermissionResult
```

Requests `READ_CALL_LOG` permission.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to show the permission dialog |

**Returns:** [`PermissionResult`](#permissionresult)

---

### Provider Configuration

Companion members of `TrustallCallLog`, called on the class rather than on `Trustall.callLog`.

#### `setProvider`

```kotlin
fun setProvider(provider: CallLogUploadProvider?)
```

Sends uploaded call logs to the given provider. Takes effect from the next upload.

| Parameter | Type | Description |
|-----------|------|-------------|
| `provider` | `CallLogUploadProvider?` | Where to send call logs. `null` restores the built-in destination. |

---

#### `defaultProvider`

```kotlin
val defaultProvider: CallLogUploadProvider
```

The built-in Gogolook destination.

---

### CallLogUploadProvider

```kotlin
interface CallLogUploadProvider {
    suspend fun uploadCallLogs(callLogs: List<CallLog>): UploadResult
}
```

| Function | Description |
|----------|-------------|
| `uploadCallLogs(callLogs)` | Uploads the logs and reports the outcome. At most 20 per call when driven by `autoUploadCallLogs()`. |

### CallLog

| Field | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Call log ID |
| `number` | `String` | Phone number |
| `type` | [`CallType`](#calltype) | Call type |
| `date` | `Long` | Call timestamp (Unix milliseconds) |
| `duration` | `Long` | Duration in seconds |
| `region` | `String` | Number's region |
| `cacheName` | `String` | Cached display name |
| `isNew` | `Boolean` | Whether the entry is unread |

### CallType

| Value | Description |
|-------|-------------|
| `INCOMING` | Incoming call |
| `OUTGOING` | Outgoing call |
| `MISSED` | Missed call |
| `REJECTED` | Rejected call |
| `BLOCKED` | Blocked call |
| `UNKNOWN` | Unknown |

### UploadResult

| Type | Description |
|------|-------------|
| `Success` | Upload succeeded |
| `Error(code, message)` | Server error with error code and message |
| `NetworkError(exception)` | Network failure with original exception |

### PermissionResult

| Type | Description |
|------|-------------|
| `Granted` | Permission is granted |
| `ShowRationale` | Should show rationale before asking again |
| `NeverAskAgain` | User denied permanently — direct them to system settings |
| `NotSupported` | Permission not supported on this device |
