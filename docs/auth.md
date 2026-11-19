# Auth

`Trustall.auth` (`TrustallAuth`) handles device registration. It ships in `trustall-core`; no separate dependency is needed.

## Register a Device

Calling [`register()`](#register) is **optional** — the SDK will automatically register the device the first time any online feature is used. Call it explicitly only if you want the device registered before the first online call.

[`register()`](#register) is a `suspend` function and must be called from a coroutine. Call it after `Trustall.initialize()` completes:

```kotlin
coroutineScope.launch {
    when (val result = Trustall.auth.register(memberId = "")) {
        is AuthResult.Success -> {
            // Registration successful
        }
        is AuthResult.Error.ServerError -> {
            Log.e("Auth", "Server error: ${result.code}")
        }
        is AuthResult.Error.NetworkError -> {
            Log.e("Auth", "Network error", result.error)
        }
        is AuthResult.Error.UnknownError -> {
            Log.e("Auth", "Unknown error", result.error)
        }
    }
}
```

## User ID

[`getUserId()`](#getuserid) is a `suspend` function.

```kotlin
// Get user ID from the latest auth response
val userId = Trustall.auth.getUserId()
```

## Device & Region Info

```kotlin
val region   = Trustall.auth.region    // Region from the last auth response
val deviceId = Trustall.auth.deviceId  // Device identifier used for registration
```

`deviceId` is the same value as `Trustall.deviceId`. To supply your own, see [Device ID](getting-started.md#device-id) — changing it discards the current registration, and the device registers again.

---

## API Reference

**Package:** `com.gogolook.trustall.core.auth`

Access via `Trustall.auth`.

### Properties

| Name | Type | Description |
|------|------|-------------|
| `region` | `String` | Current region returned by the last auth response. |
| `deviceId` | `String` | Device identifier used for registration; same as `Trustall.deviceId`. |

### Functions

#### `register`

```kotlin
suspend fun register(memberId: String): AuthResult
```

Registers the device. Calling this is optional — the SDK registers automatically the first time any online feature is used.

| Parameter | Type | Description |
|-----------|------|-------------|
| `memberId` | `String` | Reserved. Pass an empty string. |

**Returns:** [`AuthResult`](#authresult)

---

#### `getUserId`

```kotlin
suspend fun getUserId(): String
```

Returns the user ID from the latest auth response, or an empty string.

---

### AuthResult

```kotlin
sealed interface AuthResult {
    data object Success : AuthResult

    sealed interface Error : AuthResult {
        data class ServerError(val code: Int) : Error
        data class NetworkError(val error: Throwable) : Error
        data class UnknownError(val error: Throwable) : Error
    }
}
```

| Type | Description |
|------|-------------|
| `Success` | Registration succeeded |
| `Error.ServerError` | Server returned an error; `code` is the HTTP status code |
| `Error.NetworkError` | Network failure; `error` holds the original exception |
| `Error.UnknownError` | Unexpected error |
