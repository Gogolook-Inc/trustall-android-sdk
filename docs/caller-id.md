# Caller ID

`Trustall.callerId` (`TrustallCallerId`) provides call event callbacks and number info lookup, merging data from contacts, the online database, and the offline database.

## Permissions

Declare the following permissions in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
```

## Manifest Services

The SDK registers `OmniCallScreeningService` (for blocking incoming calls) and `OmniCallRedirectionService` (for blocking outgoing calls) by default. If you don't need either feature, remove them via manifest node removal:

```xml
<!-- Do not need to block incoming calls -->
<service
    android:name="com.gogolook.trustall.callerid.OmniCallScreeningService"
    tools:node="remove" />

<!-- Do not need to block outgoing calls -->
<service
    android:name="com.gogolook.trustall.callerid.OmniCallRedirectionService"
    tools:node="remove" />
```

## Call Event Callbacks

Implement [`CallsCallback`](#callscallback) and pass it to [`setCallsCallback()`](#setcallscallback):

```kotlin
Trustall.callerId.setCallsCallback(object : CallsCallback {
    override fun onScreenCall(event: CallEvent.ScreenCall) {
        // Call Screening intercept point — decide whether to reject the incoming call.
        // You can combine this with Trustall.numberBlock or check the offline spam level:
        //   Trustall.numberBlock.isBlocked(event.number)
        //   offlineInfo?.spamLevel == OfflineNumberInfo.SpamLevel.TOP
        event.updateCall(
            CallResponse(
                rejectCall  = true,
                silenceCall = true,
            )
        )
    }

    override fun onIncomingCall(event: CallEvent.IncomingCall) {
        Log.d("CallerID", "Incoming: ${event.number}")
    }

    override fun onPlaceCall(event: CallEvent.PlaceCall) {
        // Intercept before dialing — can cancel the outgoing call
        event.updateCall(PlaceCallResponse.PlaceCallUnmodified)
    }

    override fun onOutgoingCall(event: CallEvent.OutgoingCall) {
        Log.d("CallerID", "Outgoing: ${event.number}")
    }

    override fun onOffhookCall(event: CallEvent.OffhookCall) {
        Log.d("CallerID", "Offhook: ${event.number}")
    }

    override fun onIdleCall(event: CallEvent.IdleCall) {
        Log.d("CallerID", "Idle: ${event.number}")
    }
})
```

> **Tip — Blocking calls with `onScreenCall`:**
> `onScreenCall` is the recommended place to reject unwanted calls. Note that Android requires a response within **5 seconds** — any decision that takes longer will be ignored by the system.
>
> For this reason, **avoid using `Trustall.numberSearch` here**. Number Search typically hits the network, which may not complete within the time limit.
>
> Prefer these local, fast alternatives instead:
> - **Number Block module** — check `Trustall.numberBlock.isBlocked(event.number)` to reject numbers the user has explicitly blocked.
> - **Offline DB spam level** — check `Trustall.offlineDb.getNumberInfo(event.number)?.spamLevel == OfflineNumberInfo.SpamLevel.CONFIRMED` to automatically reject confirmed spam numbers with no network request.

## Number Info Lookup

[`getNumberInfo()`](#getnumberinfo) returns a `Flow<NumberInfoState>` that progressively emits results from three sources: **Contact** (`trustall-contact`), **Offline DB** (`trustall-offlinedb`), and **Number Search** (`trustall-numbersearch`). Results are merged with name priority: contact > online > offline.

If you only need data from a single source, use the corresponding module directly instead.

```kotlin
lifecycleScope.launch {
    Trustall.callerId.getNumberInfo("+886912345678").collect { state ->
        when (state) {
            is NumberInfoState.Loading -> { /* show loading */ }
            is NumberInfoState.Partial -> showNumberInfo(state.numberInfo)
            is NumberInfoState.Finish  -> showNumberInfo(state.numberInfo)
        }
    }
}
```

## Permission Checks & Requests

```kotlin
// Check
val hasPhone     = Trustall.callerId.hasPhonePermissions()
val hasCallLog   = Trustall.callerId.hasCallLogPermissions()
val hasScreening = Trustall.callerId.isCallScreeningRoleHeld()
val hasRedirect  = Trustall.callerId.isCallRedirectionRoleHeld()
val canOverlay   = Trustall.callerId.canDrawOverlays()

// Request
val result = Trustall.callerId.requestPhonePermissions(activity)
val result = Trustall.callerId.requestCallLogPermissions(activity)
val result = Trustall.callerId.requestCallScreeningRole(activity)
val result = Trustall.callerId.requestCallRedirectionRole(activity)

// Open system settings
Trustall.callerId.launchAppDetailsSettings(activity)
Trustall.callerId.launchManageDefaultAppsSettings(activity)
Trustall.callerId.launchDrawOverlaysSettings(activity)
```

---

## API Reference

**Package:** `com.gogolook.trustall.callerid`

Access via `Trustall.callerId`.

### Functions

#### `setCallsCallback`

```kotlin
fun setCallsCallback(callsCallback: CallsCallback)
```

Registers a callback to receive incoming and outgoing call events.

| Parameter | Type | Description |
|-----------|------|-------------|
| `callsCallback` | [`CallsCallback`](#callscallback) | Callback implementation |

---

#### `getNumberInfo`

```kotlin
fun getNumberInfo(number: String): Flow<NumberInfoState>
```

Returns a `Flow` that emits number info progressively from three async sources: contact book, online search, and offline database. Emits `Loading` immediately, then `Partial` as each source returns, and finally `Finish` when all sources complete. Contact data is skipped if `READ_CONTACTS` is not granted.

| Parameter | Type | Description |
|-----------|------|-------------|
| `number` | `String` | Phone number to look up (any format accepted) |

**Returns:** `Flow<`[`NumberInfoState`](#numberinfostate)`>`

---

#### `hasPhonePermissions`

```kotlin
fun hasPhonePermissions(): Boolean
```

Returns `true` if `READ_PHONE_STATE` is granted.

---

#### `hasCallLogPermissions`

```kotlin
fun hasCallLogPermissions(): Boolean
```

Returns `true` if both `READ_CALL_LOG` and `PROCESS_OUTGOING_CALLS` are granted.

---

#### `isCallScreeningRoleHeld`

```kotlin
fun isCallScreeningRoleHeld(): Boolean
```

Returns `true` if the app currently holds the `ROLE_CALL_SCREENING` role.

---

#### `isCallRedirectionRoleHeld`

```kotlin
fun isCallRedirectionRoleHeld(): Boolean
```

Returns `true` if the app currently holds the `ROLE_CALL_REDIRECTION` role.

---

#### `canDrawOverlays`

```kotlin
fun canDrawOverlays(): Boolean
```

Returns `true` if the app is allowed to draw over other apps.

---

#### `requestPhonePermissions`

```kotlin
suspend fun requestPhonePermissions(activity: ComponentActivity): PermissionResult
```

Requests `READ_PHONE_STATE` permission.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to show the permission dialog |

**Returns:** [`PermissionResult`](#permissionresult)

---

#### `requestCallLogPermissions`

```kotlin
suspend fun requestCallLogPermissions(activity: ComponentActivity): PermissionResult
```

Requests `READ_CALL_LOG` and `PROCESS_OUTGOING_CALLS` permissions. Returns the result for `READ_CALL_LOG`.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to show the permission dialog |

**Returns:** [`PermissionResult`](#permissionresult)

---

#### `requestCallScreeningRole`

```kotlin
suspend fun requestCallScreeningRole(activity: ComponentActivity): PermissionResult
```

Requests the `ROLE_CALL_SCREENING` role, which enables the app to screen and block incoming calls.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to show the role request dialog |

**Returns:** [`PermissionResult`](#permissionresult)

---

#### `requestCallRedirectionRole`

```kotlin
suspend fun requestCallRedirectionRole(activity: ComponentActivity): PermissionResult
```

Requests the `ROLE_CALL_REDIRECTION` role, which enables the app to redirect outgoing calls.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to show the role request dialog |

**Returns:** [`PermissionResult`](#permissionresult)

---

#### `launchAppDetailsSettings`

```kotlin
suspend fun launchAppDetailsSettings(activity: ComponentActivity)
```

Opens the system App Details settings screen for this app. Useful when a permission has been permanently denied and must be granted manually.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to start the settings intent |

---

#### `launchManageDefaultAppsSettings`

```kotlin
suspend fun launchManageDefaultAppsSettings(activity: ComponentActivity)
```

Opens the system Manage Default Apps settings screen. Useful for guiding the user to assign or revoke default app roles (e.g. call screening).

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to start the settings intent |

---

#### `launchDrawOverlaysSettings`

```kotlin
suspend fun launchDrawOverlaysSettings(activity: ComponentActivity)
```

Opens the system Draw Over Other Apps settings screen for this app. Required when [`canDrawOverlays()`](#candrawoverlays) returns `false` and an in-call overlay UI is needed.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to start the settings intent |

---

### CallsCallback

Interface for receiving call events. Implement and pass to [`setCallsCallback()`](#setcallscallback).

| Method | Event | Description |
|--------|-------|-------------|
| `onScreenCall(event)` | `CallEvent.ScreenCall` | Incoming call screening intercept — call `event.updateCall()` within 5 seconds |
| `onIncomingCall(event)` | `CallEvent.IncomingCall` | Incoming call notification |
| `onPlaceCall(event)` | `CallEvent.PlaceCall` | Outgoing call intercept before dialing |
| `onOutgoingCall(event)` | `CallEvent.OutgoingCall` | Outgoing call notification |
| `onOffhookCall(event)` | `CallEvent.OffhookCall` | Call connected |
| `onIdleCall(event)` | `CallEvent.IdleCall` | Call ended |

### CallEvent

```kotlin
sealed class CallEvent(open val number: String) {
    data class ScreenCall(
        override val number: String,
        val updateCall: (CallResponse) -> Unit,
    ) : CallEvent(number)

    data class IncomingCall(override val number: String) : CallEvent(number)

    data class PlaceCall(
        override val number: String,
        val updateCall: (PlaceCallResponse) -> Unit,
    ) : CallEvent(number)

    data class OutgoingCall(override val number: String) : CallEvent(number)
    data class OffhookCall(override val number: String)  : CallEvent(number)
    data class IdleCall(override val number: String)     : CallEvent(number)
}
```

### CallResponse

Passed to `ScreenCall.updateCall()` to control how the system handles the call:

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `disallowCall` | `Boolean` | `false` | Disallow the call |
| `rejectCall` | `Boolean` | `false` | Reject and send a busy signal |
| `silenceCall` | `Boolean` | `false` | Silence the ringtone |
| `skipCallLog` | `Boolean` | `false` | Do not add to call log |
| `skipNotification` | `Boolean` | `false` | Suppress the incoming call notification |

### PlaceCallResponse

Passed to `PlaceCall.updateCall()`:

| Type | Description |
|------|-------------|
| `PlaceCallUnmodified` | Proceed with the call unmodified |
| `CancelCall` | Cancel the outgoing call |

### NumberInfo

| Field | Type | Description |
|-------|------|-------------|
| `number` | `String` | Phone number |
| `name` | `String` | Identified name (priority: contact > online > offline) |
| `bizCategory` | `String` | Business category tag — see [Number Categories](./number-categories.md#business-categories) |
| `spamCategory` | `String` | Spam category tag — see [Number Categories](./number-categories.md#spam-categories) |
| `spamLevel` | [`SpamLevel`](#numberinfospamlevel) | Spam level |
| `isContact` | `Boolean` | Whether the number exists in local contacts |

### NumberInfo.SpamLevel

| Value | Description |
|-------|-------------|
| `UNLIKELY` | No spam record |
| `SUSPICIOUS` | Possibly spam |
| `CONFIRMED` | Confirmed spam |

### NumberInfoState

| Type | Description |
|------|-------------|
| `Loading` | Query started, no data yet |
| `Partial(numberInfo)` | Partial data available, more sources pending |
| `Finish(numberInfo)` | All sources have returned |

### PermissionResult

| Type | Description |
|------|-------------|
| `Granted` | Permission is granted |
| `ShowRationale` | Should show rationale before asking again |
| `NeverAskAgain` | User denied permanently — direct them to system settings |
| `NotSupported` | Permission not supported on this device |
