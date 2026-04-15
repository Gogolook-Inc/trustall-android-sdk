# SMS Log

`Trustall.smsLog` (`TrustallSmsLog`) reads SMS and MMS logs from the device.

## Permission

```xml
<uses-permission android:name="android.permission.READ_SMS" />
```

Call [`requestSmsLogPermission()`](#requestsmslogpermission) to request the permission at runtime:

```kotlin
when (Trustall.smsLog.requestSmsLogPermission(activity)) {
    is PermissionResult.Granted        -> { /* proceed */ }
    is PermissionResult.ShowRationale  -> { /* explain why the permission is needed, then ask again */ }
    is PermissionResult.NeverAskAgain  -> { /* direct the user to system settings */ }
    is PermissionResult.NotSupported   -> { /* permission not supported on this device */ }
}
```

## Retrieve SMS / MMS Logs

[`getSmsLogs()`](#getsmslogs) accepts an optional time range:

```kotlin
// Get all logs
val logs = Trustall.smsLog.getSmsLogs()

// Get logs within a time range
val logs = Trustall.smsLog.getSmsLogs(
    startTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
    endTime   = System.currentTimeMillis(),
)

logs.forEach { log ->
    Log.d("SmsLog", "From: ${log.displayAddress}")
    Log.d("SmsLog", "Body: ${log.displayBody}")
    Log.d("SmsLog", "Incoming: ${log.isIncoming}")
}
```

## Check Permission

```kotlin
val hasPermission = Trustall.smsLog.hasSmsLogPermission()
```

---

## API Reference

**Package:** `com.gogolook.trustall.smslog`

Access via `Trustall.smsLog`.

### Functions

#### `getSmsLogs`

```kotlin
suspend fun getSmsLogs(startTime: Long? = null, endTime: Long? = null): List<SmsLog>
```

Returns SMS and MMS logs within an optional time range.

| Parameter | Type | Description |
|-----------|------|-------------|
| `startTime` | `Long?` | Start time in Unix milliseconds (inclusive). Omit to start from the beginning. |
| `endTime` | `Long?` | End time in Unix milliseconds (inclusive). Omit to include up to the latest. |

**Returns:** `List<`[`SmsLog`](#smslog)`>`

---

#### `hasSmsLogPermission`

```kotlin
fun hasSmsLogPermission(): Boolean
```

Returns `true` if `READ_SMS` is granted.

---

#### `requestSmsLogPermission`

```kotlin
suspend fun requestSmsLogPermission(activity: ComponentActivity): PermissionResult
```

Requests `READ_SMS` permission.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to show the permission dialog |

**Returns:** [`PermissionResult`](#permissionresult)

---

### SmsLog

`SmsLog` is a sealed class. Common properties shared by all subtypes:

| Field | Type | Description |
|-------|------|-------------|
| `rawId` | `Long` | System raw ID |
| `threadId` | `Long` | Conversation thread ID |
| `date` | `Long` | Timestamp (Unix milliseconds) |
| `read` | `Boolean` | Whether the message has been read |
| `id` | `String` | Unique identifier (`"Sms_{rawId}"` or `"Mms_{rawId}"`) |
| `displayAddress` | `String` | Sender or recipient number for display |
| `displayBody` | `String` | Message body for display |
| `isIncoming` | `Boolean` | Whether the message was received |
| `isOutgoing` | `Boolean` | Whether the message was sent |

### SmsLog.Sms

| Field | Type | Description |
|-------|------|-------------|
| `address` | `String` | Counterpart's phone number |
| `body` | `String` | Message body |
| `type` | [`SmsType`](#smstype) | Message type |
| `serviceCenter` | `String?` | SMS service center number |

### SmsType

| Value | Description |
|-------|-------------|
| `INCOMING` | Received |
| `OUTGOING` | Sent |
| `UNKNOWN` | Unknown |

### SmsLog.Mms

| Field | Type | Description |
|-------|------|-------------|
| `addrs` | `List<`[`MmsAddr`](#mmsaddr)`>` | List of sender/recipient addresses |
| `type` | [`MmsType`](#mmstype) | Message type |
| `attachments` | `List<`[`MmsAttachment`](#mmsattachment)`>` | List of attachments |

### MmsType

| Value | Description |
|-------|-------------|
| `INCOMING` | Received |
| `OUTGOING` | Sent |
| `UNKNOWN` | Unknown |

### MmsAddr

| Field | Type | Description |
|-------|------|-------------|
| `type` | [`AddrType`](#addrtype) | Address type |
| `address` | `String` | Phone number or email address |

### AddrType

| Value | Description |
|-------|-------------|
| `From` | Sender |
| `To` | Recipient |
| `Cc` | Carbon copy |
| `Bcc` | Blind carbon copy |
| `Unknown` | Unknown |

### MmsAttachment

| Field | Type | Description |
|-------|------|-------------|
| `mimeType` | `String` | MIME type (e.g. `"text/plain"`, `"image/jpeg"`) |
| `text` | `String?` | Text content (only set for `text/plain`) |

### PermissionResult

| Type | Description |
|------|-------------|
| `Granted` | Permission is granted |
| `ShowRationale` | Should show rationale before asking again |
| `NeverAskAgain` | User denied permanently — direct them to system settings |
| `NotSupported` | Permission not supported on this device |
