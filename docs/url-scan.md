# URL Scan

`Trustall.urlScan` (`TrustallUrlScan`) scans URLs for malicious or suspicious content.

## Scan a URL

Call [`scan()`](#scan) with a URL string:

```kotlin
when (val result = Trustall.urlScan.scan("https://example.com")) {
    is UrlScanResult.Success -> {
        when (result.level) {
            Level.SAFE       -> Log.d("UrlScan", "Safe")
            Level.SUSPICIOUS -> Log.w("UrlScan", "Suspicious")
            Level.MALICIOUS  -> Log.e("UrlScan", "Malicious!")
            Level.UNDEFINED  -> Log.d("UrlScan", "Undefined")
        }
    }
    is UrlScanResult.Error -> {
        Log.e("UrlScan", "Scan error for ${result.url}", result.error)
    }
}
```

## Cache Policy

The default is `CachePolicy.NO_CACHE`, which always performs a network request. When a [`CachePolicy`](#cachepolicy) is provided, a cached result is returned if one exists **and was stored within the specified max age**. If the cached result is older than the max age, a fresh network request is made.

```kotlin
// Accept a cached result stored within the last 30 minutes
val result = Trustall.urlScan.scan(
    url = "https://example.com",
    cachePolicy = CachePolicy.minute(30),
)

// Accept a cached result stored within the last 2 hours
val result = Trustall.urlScan.scan(
    url = "https://example.com",
    cachePolicy = CachePolicy.hour(2),
)

// Accept a cached result stored within the last day
val result = Trustall.urlScan.scan(
    url = "https://example.com",
    cachePolicy = CachePolicy.day(1),
)
```

Custom max age:

```kotlin
val policy = CachePolicy(
    allowCache = true,
    maxAgeMillis = 15 * 60 * 1000L, // accept cache up to 15 minutes old
)
```

## Scan URLs in Text

[`scanText()`](#scantext) extracts every URL from a piece of text — an SMS body, a chat message — and scans them concurrently, returning one result per distinct URL. Texts without URLs trigger no network call.

```kotlin
val results = Trustall.urlScan.scanText(
    text = "Claim your prize at http://example.com/win",
    cachePolicy = CachePolicy.day(1),
)
results.forEach { result ->
    when (result) {
        is UrlScanResult.Success -> Log.d("UrlScan", "${result.url}: ${result.level}")
        is UrlScanResult.Error   -> Log.e("UrlScan", "Scan error for ${result.url}", result.error)
    }
}
```

To extract without scanning, use [`extractUrls()`](#extracturls):

```kotlin
val urls = Trustall.urlScan.extractUrls("check http://a.com and b.com/path")
// ["http://a.com", "b.com/path"]
```

Pairs naturally with [SMS Flow](sms-flow.md) to scan every incoming SMS for malicious links.

## Custom Providers

Implement [`UrlScanProvider`](#urlscanprovider) to scan URLs against your own backend, and register it with [`setProviders()`](#setproviders) before `Trustall.initialize()`:

```kotlin
class MyProvider : UrlScanProvider {
    override suspend fun scan(url: String, cachePolicy: CachePolicy): UrlScanResult? =
        UrlScanResult.Success(url, myBackend.classify(url))
}

TrustallUrlScan.setProviders(listOf(MyProvider()))
```

How the list is consulted:

- `scan()` asks each provider in order and returns the first `UrlScanResult.Success`.
- A provider that returns `UrlScanResult.Error` or throws is skipped. The first such error is returned only if no later provider succeeds.
- A provider that returns `null` does not cover that URL and is skipped without counting as an error. If no provider covers the URL, `scan()` returns an `Error` whose `error` is an `IllegalStateException`.
- `cachePolicy` is passed through as the caller's request. The SDK keeps no cache in front of providers, so honour it if your provider caches.
- To keep Gogolook's scanner as a fallback, put `TrustallUrlScan.defaultProvider` last.
- `setProviders(null)` restores Gogolook's scanner as the only source. `setProviders(emptyList())` leaves no source, so every scan returns an `Error`.

`scanText()` walks the same list once per URL, so anything built on it — such as scanning incoming messages from [SMS Flow](sms-flow.md) — uses your providers too. `extractUrls()` runs on the device and is unaffected.

See [Custom Providers](getting-started.md#custom-providers) for the rules common to every provider.

---

## API Reference

**Package:** `com.gogolook.trustall.core.urlscan`

Access via `Trustall.urlScan`.

### Functions

#### `scan`

```kotlin
suspend fun scan(url: String, cachePolicy: CachePolicy = CachePolicy.NO_CACHE): UrlScanResult
```

Scans the given URL for threats.

| Parameter | Type | Description |
|-----------|------|-------------|
| `url` | `String` | The URL to scan |
| `cachePolicy` | [`CachePolicy`](#cachepolicy) | Cache behaviour; defaults to `CachePolicy.NO_CACHE` |

**Returns:** [`UrlScanResult`](#urlscanresult)

---

#### `extractUrls`

```kotlin
fun extractUrls(text: String): List<String>
```

Extracts web URLs from arbitrary text. Matches are returned verbatim (scheme-less matches such as `example.com/path` are not rewritten), deduplicated, in order of first appearance. The domain part of email addresses is skipped.

| Parameter | Type | Description |
|-----------|------|-------------|
| `text` | `String` | The text to search for URLs |

**Returns:** `List<String>`

---

#### `scanText`

```kotlin
suspend fun scanText(text: String, cachePolicy: CachePolicy = CachePolicy.NO_CACHE): List<UrlScanResult>
```

Extracts URLs from the text via [`extractUrls()`](#extracturls) and scans them concurrently.

| Parameter | Type | Description |
|-----------|------|-------------|
| `text` | `String` | The text (e.g. an SMS body) to extract URLs from |
| `cachePolicy` | [`CachePolicy`](#cachepolicy) | Cache behaviour applied to each scan; defaults to `CachePolicy.NO_CACHE` |

**Returns:** `List<`[`UrlScanResult`](#urlscanresult)`>` — one result per distinct URL found; empty when the text contains no URL

---

### Provider Configuration

Companion members of `TrustallUrlScan`, called on the class rather than on `Trustall.urlScan`.

#### `setProviders`

```kotlin
fun setProviders(providers: List<UrlScanProvider>?)
```

Sets the ordered list of providers used for scans, replacing any previous one. Takes effect from the next call.

| Parameter | Type | Description |
|-----------|------|-------------|
| `providers` | `List<UrlScanProvider>?` | Providers in priority order. `null` restores the built-in provider; an empty list leaves no source. |

---

#### `defaultProvider`

```kotlin
val defaultProvider: UrlScanProvider
```

The built-in Gogolook provider. Put it last in the list to keep it as a fallback.

---

### UrlScanProvider

```kotlin
interface UrlScanProvider {
    suspend fun scan(url: String, cachePolicy: CachePolicy): UrlScanResult?
}
```

| Function | Description |
|----------|-------------|
| `scan(url, cachePolicy)` | Returns `Success` to end the chain, `Error` to move on (returned if nothing later succeeds), or `null` when this provider does not cover the URL. |

### UrlScanResult

| Type | Description |
|------|-------------|
| `Success(url, level)` | Scan succeeded; `level` is the [`Level`](#level) |
| `Error(url, error)` | Scan failed; `error` holds the original exception |

### Level

| Value | Description |
|-------|-------------|
| `SAFE` | Safe |
| `SUSPICIOUS` | Suspicious — consider warning the user |
| `MALICIOUS` | Malicious — consider blocking |
| `UNDEFINED` | Could not be determined |

### CachePolicy

| Factory / Field | Description |
|----------------|-------------|
| `CachePolicy.NO_CACHE` | Always fetch from network (default) |
| `CachePolicy.minute(n)` | Accept cache up to `n` minutes old |
| `CachePolicy.hour(n)` | Accept cache up to `n` hours old |
| `CachePolicy.day(n)` | Accept cache up to `n` days old |
| `CachePolicy(allowCache, maxAgeMillis)` | Custom policy with explicit max age in milliseconds |
