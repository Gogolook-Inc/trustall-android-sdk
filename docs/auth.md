# Auth

`Trustall.auth` (`TrustallAuth`) handles device registration and member ID management.

## Register a Device

Calling [`register()`](#register) is **optional** — the SDK will automatically register the device the first time any online feature is used. Call it explicitly only if you want to associate a `memberId` with the device upfront. If the `memberId` is not available at initialization time, you can also call [`setMemberId()`](#setmemberid) later.

[`register()`](#register) is a `suspend` function and must be called from a coroutine. Call it after `Trustall.initialize()` completes:

```kotlin
coroutineScope.launch {
    when (val result = Trustall.auth.register(memberId = "user-123")) {
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

`memberId` is a custom identifier that lets you associate the device with a user in your own system (e.g. an account ID or username). If you don't need this, pass an empty string:

```kotlin
Trustall.auth.register(memberId = "")
```

## Member ID Management

[`setMemberId()`](#setmemberid), [`getMemberId()`](#getmemberid), and [`getUserId()`](#getuserid) are all `suspend` functions.

```kotlin
// Set member ID
Trustall.auth.setMemberId("user-123")

// Get stored member ID
val memberId = Trustall.auth.getMemberId()

// Get user ID from the latest auth response
val userId = Trustall.auth.getUserId()
```

## Device & Region Info

```kotlin
val region   = Trustall.auth.region    // Region from the last auth response
val deviceId = Trustall.auth.deviceId  // Device identifier used for registration
```

---

## API Reference

**Package:** `com.gogolook.trustall.core.auth`

Access via `Trustall.auth`.

### Properties

| Name | Type | Description |
|------|------|-------------|
| `region` | `String` | Current region returned by the last auth response. |
| `deviceId` | `String` | Device identifier used for registration. |

### Functions

#### `register`

```kotlin
suspend fun register(memberId: String): AuthResult
```

Registers the device with the given member ID. Calling this is optional — the SDK registers automatically the first time any online feature is used.

| Parameter | Type | Description |
|-----------|------|-------------|
| `memberId` | `String` | Partner-assigned member identifier. Pass an empty string if not needed. |

**Returns:** [`AuthResult`](#authresult)

---

#### `getMemberId`

```kotlin
suspend fun getMemberId(): String
```

Returns the stored member ID, or an empty string if not set.

---

#### `setMemberId`

```kotlin
suspend fun setMemberId(memberId: String)
```

Stores the given member ID locally.

| Parameter | Type | Description |
|-----------|------|-------------|
| `memberId` | `String` | Member ID to store. |

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
