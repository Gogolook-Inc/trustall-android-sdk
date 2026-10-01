# Offline DB

`Trustall.offlineDb` (`TrustallOfflineDb`) provides a local offline number database for instant lookups without a network connection. The database is downloaded per region and maintained automatically.

## Download / Update the Database

[`downloadIfNeeded()`](#downloadifneeded) returns a `Flow<DownloadState>` and only downloads when an update is required:

```kotlin
lifecycleScope.launch {
    Trustall.offlineDb.downloadIfNeeded().collect { state ->
        when (state) {
            is DownloadState.Downloading -> {
                Log.d("OfflineDb", "Progress: ${state.progress}%")
            }
            is DownloadState.Finished -> {
                Log.d("OfflineDb", "Download complete")
            }
            is DownloadState.Failed -> {
                Log.e("OfflineDb", "Failed: ${state.reason}", state.error)
            }
        }
    }
}
```

## Look Up a Number

[`getNumberInfo()`](#getnumberinfo) returns an [`OfflineNumberInfo`](#offlinenumberinfo) if found, or `null`:

```kotlin
val info = Trustall.offlineDb.getNumberInfo("+886912345678")
if (info != null) {
    Log.d("OfflineDb", "Name: ${info.name}, Spam: ${info.spamLevel}")
}
```

## Database Info

[`getDbProfile()`](#getdbprofile) returns the current [`OfflineDbProfile`](#offlinedbprofile), or `null` if the database has not been downloaded:

```kotlin
val profile = Trustall.offlineDb.getDbProfile()
if (profile != null) {
    Log.d("OfflineDb", "Version: ${profile.version}, Spam entries: ${profile.spamNumSize}")
}
```

## Clear the Local Database

```kotlin
Trustall.offlineDb.clear()
```

## Custom Provider

Implement [`OfflineDbProvider`](#offlinedbprovider) to back offline lookups with your own database — one shipped inside the app or downloaded from your own servers — and register it with [`setProvider()`](#setprovider) before `Trustall.initialize()`:

```kotlin
class MyOfflineDb : OfflineDbProvider {
    override suspend fun getNumberInfo(number: String): OfflineNumberInfo? =
        myBundledDb.lookup(number)
}

TrustallOfflineDb.setProvider(MyOfflineDb())
```

Only `getNumberInfo()` is required. The other functions default to what suits a database shipped inside the app: `downloadIfNeeded()` emits `Finished` at once, `clear()` does nothing and `getDbProfile()` returns `null`. Override them if your database is downloaded.

- A single provider replaces the whole database: `downloadIfNeeded()`, `getNumberInfo()`, `getDbProfile()` and `clear()` on `Trustall.offlineDb` all go to it. There is no chain; merge several sources inside one implementation if needed.
- `setProvider(null)` restores Gogolook's database.
- Registering before `initialize()` means Gogolook's database is never downloaded. Switching later leaves any file already downloaded in place; call `TrustallOfflineDb.defaultProvider.clear()` to delete it.
- A throw inside `getNumberInfo()` is logged and yields `null`. A throw inside `downloadIfNeeded()` is reported as `DownloadState.Failed(Reason.UNKNOWN)`; emit `Failed` yourself to keep the specific `Reason`.

The provider also backs the offline source of [`Trustall.callerId.getNumberInfo()`](caller-id.md#number-info-lookup) and the offline spam-level check suggested for [call screening](caller-id.md#call-event-callbacks). `getNumberInfo()` must not block the caller — Caller ID queries it while the phone is ringing — so move file and database I/O to your own dispatcher. See [Custom Providers](getting-started.md#custom-providers) for the rules common to every provider.

---

## API Reference

**Package:** `com.gogolook.trustall.core.offlinedb`

Access via `Trustall.offlineDb`.

### Functions

#### `downloadIfNeeded`

```kotlin
fun downloadIfNeeded(): Flow<DownloadState>
```

Downloads or refreshes the offline database for the current region if needed. Emits [`DownloadState`](#downloadstate) updates until completion or failure. No download is initiated if the database is already up to date.

**Returns:** `Flow<`[`DownloadState`](#downloadstate)`>`

---

#### `getNumberInfo`

```kotlin
suspend fun getNumberInfo(number: String): OfflineNumberInfo?
```

Queries the offline database for number information.

| Parameter | Type | Description |
|-----------|------|-------------|
| `number` | `String` | Phone number to look up |

**Returns:** [`OfflineNumberInfo`](#offlinenumberinfo)`?` — `null` if not found or database not downloaded.

---

#### `getDbProfile`

```kotlin
suspend fun getDbProfile(): OfflineDbProfile?
```

Returns the current offline database profile, or `null` if the database has not been downloaded yet.

**Returns:** [`OfflineDbProfile`](#offlinedbprofile)`?`

---

#### `clear`

```kotlin
suspend fun clear()
```

Clears the local offline database.

---

### Provider Configuration

Companion members of `TrustallOfflineDb`, called on the class rather than on `Trustall.offlineDb`.

#### `setProvider`

```kotlin
fun setProvider(provider: OfflineDbProvider?)
```

Replaces the offline database with the given provider. Takes effect from the next call.

| Parameter | Type | Description |
|-----------|------|-------------|
| `provider` | `OfflineDbProvider?` | The database to use. `null` restores the built-in database. |

---

#### `defaultProvider`

```kotlin
val defaultProvider: OfflineDbProvider
```

The built-in Gogolook database.

---

### OfflineDbProvider

```kotlin
interface OfflineDbProvider {
    suspend fun getNumberInfo(number: String): OfflineNumberInfo?
    fun downloadIfNeeded(): Flow<DownloadState> = flowOf(DownloadState.Finished)
    suspend fun clear() {}
    suspend fun getDbProfile(): OfflineDbProfile? = null
}
```

| Function | Description |
|----------|-------------|
| `getNumberInfo(number)` | Returns what the database knows about the number, or `null`. |
| `downloadIfNeeded()` | Optional. Emits progress until the download finishes or fails. |
| `clear()` | Optional. Deletes the local database. |
| `getDbProfile()` | Optional. Returns the profile of the database in place, or `null`. |

### OfflineNumberInfo

| Field | Type | Description |
|-------|------|-------------|
| `number` | `String` | Phone number |
| `name` | `String` | Identified name |
| `spamCategory` | `String` | Spam category tag — see [Number Categories](./number-categories.md#spam-categories) |
| `spamLevel` | [`SpamLevel`](#offlinenumberinfospamlevel) | Spam level |

### OfflineNumberInfo.SpamLevel

| Value | Description |
|-------|-------------|
| `UNLIKELY` | No spam record |
| `SUSPICIOUS` | Possibly spam |
| `CONFIRMED` | Confirmed spam |

### OfflineDbProfile

| Field | Type | Description |
|-------|------|-------------|
| `version` | `Int` | Database version number |
| `topNumSize` | `Int` | Number of TOP-level spam entries |
| `toptopSpamSize` | `Int` | Number of highest-level spam entries |
| `spamNumSize` | `Int` | Total number of spam entries |

### DownloadState

| Type | Description |
|------|-------------|
| `Downloading(progress)` | Downloading; `progress` is 0–100 |
| `Finished` | Download complete |
| `Failed(reason, error?)` | Failed; `reason` is a [`Reason`](#reason) enum, `error` is the original exception (nullable) |

### Reason

| Value | Description |
|-------|-------------|
| `UP_TO_DATE` | Database is already up to date; no download needed |
| `NULL_DB_PROFILE` | Could not fetch the database profile |
| `INVALID_PARAMETER` | Invalid parameter |
| `DOWNLOAD_SAVE_FILE_FAILED` | Failed to save the downloaded file |
| `CHECK_MD5_MISMATCHED_MD5` | MD5 checksum mismatch |
| `UNZIP_FILE_ERROR` | Decompression failed |
| `PATCH_OUT_OF_MEMORY` | Out of memory during patching |
| `UNKNOWN` | Unknown error |
