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
