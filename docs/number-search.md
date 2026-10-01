# Number Search

`Trustall.numberSearch` (`TrustallNumberSearch`) looks up number information online with cache support.

## Look Up a Number

Pass a phone number in E.164 format to [`getNumberInfo()`](#getnumberinfo) (e.g. `"+886912345678"`). Returns `null` on failure:

```kotlin
val info = Trustall.numberSearch.getNumberInfo(e164 = "+886912345678")
if (info != null) {
    Log.d("Search", "Name: ${info.name}")
    Log.d("Search", "Spam level: ${info.spamLevel}")
    Log.d("Search", "Biz category: ${info.bizCategory}")
    Log.d("Search", "Spam category: ${info.spamCategory}")
}
```

## Force Refresh (Skip Cache)

```kotlin
val info = Trustall.numberSearch.getNumberInfo(
    e164 = "+886912345678",
    isForceUpdate = true,
)
```

## Cache Management

```kotlin
// Clear cache for specific numbers; returns the number of entries cleared
val cleared = Trustall.numberSearch.clearCache("+886912345678", "+886987654321")

// Clear all cache
Trustall.numberSearch.clearCache()

// Remove expired cache entries
Trustall.numberSearch.removeExpiredCache()
```

## Custom Providers

Implement [`NumberSearchProvider`](#numbersearchprovider) to serve lookups from your own backend, and register it with [`setProviders()`](#setproviders) before `Trustall.initialize()`:

```kotlin
class MyProvider : NumberSearchProvider {
    override suspend fun getNumberInfo(e164: String, isForceUpdate: Boolean): OnlineNumberInfo? =
        myBackend.lookup(e164)?.let { OnlineNumberInfo(number = e164, name = it.name) }
}

TrustallNumberSearch.setProviders(listOf(MyProvider()))
```

Only `getNumberInfo()` is required. `OnlineNumberInfo` needs just `number`; every other field defaults to "no data".

How the list is consulted:

- `getNumberInfo()` asks each provider in order and returns the first non-`null` result. A provider that returns `null` or throws is skipped (throws are logged). If every provider is skipped, the result is `null`.
- `clearCache()` and `removeExpiredCache()` are forwarded to every provider in the list.
- To keep Gogolook's search as a fallback, put `TrustallNumberSearch.defaultProvider` last.
- `setProviders(null)` restores Gogolook's search as the only source. `setProviders(emptyList())` leaves no source, so every lookup returns `null`.

The same list backs the online source of [`Trustall.callerId.getNumberInfo()`](caller-id.md#number-info-lookup), so the name, categories and spam level it reports come from your providers.

`getNumberInfo()` must not block the caller — Caller ID looks numbers up while the phone is ringing. See [Custom Providers](getting-started.md#custom-providers) for the rules common to every provider.

---

## API Reference

**Package:** `com.gogolook.trustall.core.numbersearch`

Access via `Trustall.numberSearch`.

### Functions

#### `getNumberInfo`

```kotlin
suspend fun getNumberInfo(e164: String, isForceUpdate: Boolean = false): OnlineNumberInfo?
```

Returns number info for the given E.164 phone number, or `null` on failure.

| Parameter | Type | Description |
|-----------|------|-------------|
| `e164` | `String` | Phone number in E.164 format (e.g. `"+886912345678"`) |
| `isForceUpdate` | `Boolean` | If `true`, bypasses cache and forces a network request. Defaults to `false`. |

**Returns:** [`OnlineNumberInfo`](#onlinenumberinfo)`?`

---

#### `clearCache`

```kotlin
suspend fun clearCache(vararg e164s: String): Int
```

Clears the cache for specific phone numbers.

| Parameter | Type | Description |
|-----------|------|-------------|
| `e164s` | `vararg String` | Phone numbers to clear from cache |

**Returns:** Number of cache entries cleared.

---

```kotlin
suspend fun clearCache()
```

Clears all cached number info (both memory and database cache).

---

#### `removeExpiredCache`

```kotlin
suspend fun removeExpiredCache()
```

Removes expired cache entries from the database.

---

### Provider Configuration

Companion members of `TrustallNumberSearch`, called on the class rather than on `Trustall.numberSearch`.

#### `setProviders`

```kotlin
fun setProviders(providers: List<NumberSearchProvider>?)
```

Sets the ordered list of providers used for lookups, replacing any previous one. Takes effect from the next call.

| Parameter | Type | Description |
|-----------|------|-------------|
| `providers` | `List<NumberSearchProvider>?` | Providers in priority order. `null` restores the built-in provider; an empty list leaves no source. |

---

#### `defaultProvider`

```kotlin
val defaultProvider: NumberSearchProvider
```

The built-in Gogolook provider. Put it last in the list to keep it as a fallback.

---

### NumberSearchProvider

```kotlin
interface NumberSearchProvider {
    suspend fun getNumberInfo(e164: String, isForceUpdate: Boolean): OnlineNumberInfo?
    suspend fun clearCache(vararg e164s: String): Int = 0
    suspend fun clearCache() {}
    suspend fun removeExpiredCache() {}
}
```

| Function | Description |
|----------|-------------|
| `getNumberInfo(e164, isForceUpdate)` | Returns info for the number, or `null` to pass to the next provider. `isForceUpdate` asks to bypass any cache you keep. |
| `clearCache(vararg e164s)` | Optional. Returns how many entries were removed. |
| `clearCache()` | Optional. |
| `removeExpiredCache()` | Optional. |

### OnlineNumberInfo

| Field | Type | Description |
|-------|------|-------------|
| `number` | `String` | Phone number |
| `name` | `String` | Identified name |
| `bizCategory` | `String` | Business category tag — see [Number Categories](./number-categories.md#business-categories) |
| `spamCategory` | `String` | Spam category tag — see [Number Categories](./number-categories.md#spam-categories) |
| `spamLevel` | [`SpamLevel`](#onlinenumberinfospamlevel) | Spam level |

Only `number` is required when constructing one; the other fields default to `""` and `UNLIKELY`.

### OnlineNumberInfo.SpamLevel

| Value | Description |
|-------|-------------|
| `UNLIKELY` | No spam record |
| `SUSPICIOUS` | Possibly spam |
| `CONFIRMED` | Confirmed spam |
