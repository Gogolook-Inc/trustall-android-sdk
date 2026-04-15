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

### OnlineNumberInfo

| Field | Type | Description |
|-------|------|-------------|
| `number` | `String` | Phone number |
| `name` | `String` | Identified name |
| `bizCategory` | `String` | Business category tag — see [Number Categories](./number-categories.md#business-categories) |
| `spamCategory` | `String` | Spam category tag — see [Number Categories](./number-categories.md#spam-categories) |
| `spamLevel` | [`SpamLevel`](#onlinenumberinfospamlevel) | Spam level |

### OnlineNumberInfo.SpamLevel

| Value | Description |
|-------|-------------|
| `UNLIKELY` | No spam record |
| `SUSPICIOUS` | Possibly spam |
| `CONFIRMED` | Confirmed spam |
