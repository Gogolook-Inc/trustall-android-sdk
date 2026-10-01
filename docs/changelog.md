# Release Notes

## Latest Version

[trustall-bom `2026.09.01`](#trustall-bom-20260901)

---

## 2026.09.01 — September 23, 2026 {#trustall-bom-20260901}

<details>
<summary>Module versions in this BOM</summary>

| Module | Version | Gradle Dependency |
|--------|---------|-------------------|
| `trustall-bom` | 2026.09.01 | `com.gogolook.trustall:trustall-bom` |
| `trustall-core` | 2026.09.01 | `com.gogolook.trustall:trustall-core` |
| `trustall-callerid` | 1.0.3 | `com.gogolook.trustall:trustall-callerid` |
| `trustall-calllog` | 1.1.0 | `com.gogolook.trustall:trustall-calllog` |
| `trustall-contact` | 1.0.1 | `com.gogolook.trustall:trustall-contact` |
| `trustall-msgfilter` | 1.0.2 | `com.gogolook.trustall:trustall-msgfilter` |
| `trustall-numberblock` | 1.0.2 | `com.gogolook.trustall:trustall-numberblock` |
| `trustall-numbersearch` | 1.1.0 | `com.gogolook.trustall:trustall-numbersearch` |
| `trustall-offlinedb` | 1.1.0 | `com.gogolook.trustall:trustall-offlinedb` |
| `trustall-permission` | 1.0.1 | `com.gogolook.trustall:trustall-permission` |
| `trustall-smsflow` | 1.0.0 | `com.gogolook.trustall:trustall-smsflow` |
| `trustall-smslog` | 1.0.1 | `com.gogolook.trustall:trustall-smslog` |
| `trustall-urlscan` | 1.1.0 | `com.gogolook.trustall:trustall-urlscan` |
| `network:production` | 1.0.0 | `com.gogolook.trustall.network:production` |
| `network:staging` | 1.0.0 | `com.gogolook.trustall.network:staging` |
| `network:sandbox` | 1.0.0 | `com.gogolook.trustall.network:sandbox` |

</details>

Number search, URL scan, the offline database and call log upload can now be backed by your own implementation, and the device ID can be supplied at initialization. See [Custom Providers](getting-started.md#custom-providers) and [Device ID](getting-started.md#device-id).

### trustall-core `2026.09.01`

- **Breaking:** `trustall-auth` is merged into `trustall-core` and is no longer published. Remove `com.gogolook.trustall:trustall-auth` from your dependencies; `Trustall.auth` and the `com.gogolook.trustall.core.auth` package are unchanged.
- **Breaking:** `Trustall.deviceId` is read-only.
- New `SdkConfig.deviceId`: supply your own device ID (32 lowercase hex characters) instead of the SDK-generated one. It is stored and reused on later launches. A different value discards the current registration and member ID, and the device registers again.
- `Trustall.initialize()` can be called again on a running SDK to adopt a new device ID without a relaunch.

### trustall-numbersearch `1.1.0`

- New `NumberSearchProvider` and `TrustallNumberSearch.setProviders()`: serve lookups from your own backend. Providers are consulted in order and the first non-`null` result wins; cache operations reach every provider. `TrustallNumberSearch.defaultProvider` exposes the built-in provider for use as a fallback. See [Custom Providers](number-search.md#custom-providers).
- `OnlineNumberInfo` now defaults every field except `number`.
- Changed: cancelling the calling coroutine propagates out of `getNumberInfo()` instead of returning `null`. `clearCache()` and `removeExpiredCache()` log and skip a failing provider instead of throwing.

### trustall-urlscan `1.1.0`

- New `UrlScanProvider` and `TrustallUrlScan.setProviders()`: scan against your own backend. The first `Success` wins; `Error` or a throw moves to the next provider, and `null` means the provider does not cover the URL. `scanText()` uses the same providers. `TrustallUrlScan.defaultProvider` exposes the built-in provider. See [Custom Providers](url-scan.md#custom-providers).
- Changed: cancelling the calling coroutine propagates out of `scan()` instead of returning `UrlScanResult.Error`.

### trustall-offlinedb `1.1.0`

- New `OfflineDbProvider` and `TrustallOfflineDb.setProvider()`: back offline lookups with your own database. Only `getNumberInfo()` is required. `TrustallOfflineDb.defaultProvider` exposes the built-in database. See [Custom Provider](offline-db.md#custom-provider).
- `getNumberInfo()` returns `null` instead of throwing when the lookup fails. `downloadIfNeeded()` reports a throw as `DownloadState.Failed(Reason.UNKNOWN)`.
- Fixed: a cancelled download is no longer reported as `Failed`.

### trustall-calllog `1.1.0`

- New `CallLogUploadProvider` and `TrustallCallLog.setProvider()`: send uploads to your own backend. `TrustallCallLog.defaultProvider` exposes the built-in destination. See [Custom Upload Destination](call-log.md#custom-upload-destination).
- Fixed: `autoUploadCallLogs()` re-uploaded the newest already-uploaded record on every run.
- Fixed: a cancelled upload is no longer reported as `NetworkError`.

### trustall-msgfilter `1.0.2`, trustall-numberblock `1.0.2`

- No functional change. Republished without the `trustall-auth` dependency.

---

## 2026.08.01 — August 28, 2026 {#trustall-bom-20260801}

<details>
<summary>Module versions in this BOM</summary>

| Module | Version | Gradle Dependency |
|--------|---------|-------------------|
| `trustall-bom` | 2026.08.01 | `com.gogolook.trustall:trustall-bom` |
| `trustall-core` | 2026.08.01 | `com.gogolook.trustall:trustall-core` |
| `trustall-auth` | 1.0.1 | `com.gogolook.trustall:trustall-auth` |
| `trustall-callerid` | 1.0.3 | `com.gogolook.trustall:trustall-callerid` |
| `trustall-calllog` | 1.0.1 | `com.gogolook.trustall:trustall-calllog` |
| `trustall-contact` | 1.0.1 | `com.gogolook.trustall:trustall-contact` |
| `trustall-msgfilter` | 1.0.1 | `com.gogolook.trustall:trustall-msgfilter` |
| `trustall-numberblock` | 1.0.1 | `com.gogolook.trustall:trustall-numberblock` |
| `trustall-numbersearch` | 1.0.3 | `com.gogolook.trustall:trustall-numbersearch` |
| `trustall-offlinedb` | 1.0.3 | `com.gogolook.trustall:trustall-offlinedb` |
| `trustall-permission` | 1.0.1 | `com.gogolook.trustall:trustall-permission` |
| `trustall-smsflow` | 1.0.0 | `com.gogolook.trustall:trustall-smsflow` |
| `trustall-smslog` | 1.0.1 | `com.gogolook.trustall:trustall-smslog` |
| `trustall-urlscan` | 1.0.2 | `com.gogolook.trustall:trustall-urlscan` |
| `network:production` | 1.0.0 | `com.gogolook.trustall.network:production` |
| `network:staging` | 1.0.0 | `com.gogolook.trustall.network:staging` |
| `network:sandbox` | 1.0.0 | `com.gogolook.trustall.network:sandbox` |

</details>

### trustall-smsflow `1.0.0` — new module

- Real-time incoming SMS events via `Trustall.smsFlow.incomingSms` (a hot `SharedFlow`)
- The SMS receiver is declared in the module manifest: an incoming SMS wakes the host app process even after it has been killed, and the flow replays recent messages so a collector started from `Application.onCreate()` still observes the triggering message
- Multipart segments are assembled before emission; the receiving SIM's subscription id is included
- `hasSmsReceivePermission()` / `requestSmsReceivePermission()` manage the required `RECEIVE_SMS` permission (merged into the app manifest by this module)

See [SMS Flow](sms-flow.md) for the full guide.

### trustall-urlscan `1.0.2`

- New `extractUrls(text)`: extracts web URLs from arbitrary text
- New `scanText(text, cachePolicy)`: extracts URLs from a text (e.g. an SMS body) and scans them concurrently, returning one result per distinct URL; texts without URLs trigger no network call

---

## 2026.07.01 — July 16, 2026 {#trustall-bom-20260701}

<details>
<summary>Module versions in this BOM</summary>

| Module | Version | Gradle Dependency |
|--------|---------|-------------------|
| `trustall-bom` | 2026.07.01 | `com.gogolook.trustall:trustall-bom` |
| `trustall-core` | 2026.07.01 | `com.gogolook.trustall:trustall-core` |
| `trustall-auth` | 1.0.1 | `com.gogolook.trustall:trustall-auth` |
| `trustall-callerid` | 1.0.3 | `com.gogolook.trustall:trustall-callerid` |
| `trustall-calllog` | 1.0.1 | `com.gogolook.trustall:trustall-calllog` |
| `trustall-contact` | 1.0.1 | `com.gogolook.trustall:trustall-contact` |
| `trustall-msgfilter` | 1.0.1 | `com.gogolook.trustall:trustall-msgfilter` |
| `trustall-numberblock` | 1.0.1 | `com.gogolook.trustall:trustall-numberblock` |
| `trustall-numbersearch` | 1.0.3 | `com.gogolook.trustall:trustall-numbersearch` |
| `trustall-offlinedb` | 1.0.3 | `com.gogolook.trustall:trustall-offlinedb` |
| `trustall-permission` | 1.0.1 | `com.gogolook.trustall:trustall-permission` |
| `trustall-smslog` | 1.0.1 | `com.gogolook.trustall:trustall-smslog` |
| `trustall-urlscan` | 1.0.1 | `com.gogolook.trustall:trustall-urlscan` |
| `network:production` | 1.0.0 | `com.gogolook.trustall.network:production` |
| `network:staging` | 1.0.0 | `com.gogolook.trustall.network:staging` |
| `network:sandbox` | 1.0.0 | `com.gogolook.trustall.network:sandbox` |

</details>

All published AARs now ship with their internal implementation obfuscated. The public API surface (`Trustall` / `Trustall.*` entry points, model classes, and callback interfaces) is unchanged — no integration changes are required. When reporting a crash, please include the SDK module versions in use so the stack trace can be de-obfuscated.

### trustall-auth `1.0.1`

- `user_id` and `member_id` are now stored encrypted at rest

### trustall-numbersearch `1.0.3`

- Support the new quick-reply and survey fields in the number search response (Omnidroid 2026.07.01 schema)
- `Trustall.numberSearch` initialization no longer performs blocking I/O; the search passphrase is now loaded lazily on first use

---

## 2026.04.01 — April 1, 2026 {#trustall-bom-20260401}

<details>
<summary>Module versions in this BOM</summary>

| Module | Version | Gradle Dependency |
|--------|---------|-------------------|
| `trustall-bom` | 2026.04.01 | `com.gogolook.trustall:trustall-bom` |
| `trustall-core` | 2026.04.01 | `com.gogolook.trustall:trustall-core` |
| `trustall-auth` | 1.0.0 | `com.gogolook.trustall:trustall-auth` |
| `trustall-callerid` | 1.0.2 | `com.gogolook.trustall:trustall-callerid` |
| `trustall-calllog` | 1.0.0 | `com.gogolook.trustall:trustall-calllog` |
| `trustall-contact` | 1.0.0 | `com.gogolook.trustall:trustall-contact` |
| `trustall-msgfilter` | 1.0.0 | `com.gogolook.trustall:trustall-msgfilter` |
| `trustall-numberblock` | 1.0.0 | `com.gogolook.trustall:trustall-numberblock` |
| `trustall-numbersearch` | 1.0.2 | `com.gogolook.trustall:trustall-numbersearch` |
| `trustall-offlinedb` | 1.0.2 | `com.gogolook.trustall:trustall-offlinedb` |
| `trustall-permission` | 1.0.0 | `com.gogolook.trustall:trustall-permission` |
| `trustall-smslog` | 1.0.0 | `com.gogolook.trustall:trustall-smslog` |
| `trustall-urlscan` | 1.0.0 | `com.gogolook.trustall:trustall-urlscan` |
| `network:production` | 1.0.0 | `com.gogolook.trustall.network:production` |
| `network:staging` | 1.0.0 | `com.gogolook.trustall.network:staging` |
| `network:sandbox` | 1.0.0 | `com.gogolook.trustall.network:sandbox` |

</details>

### trustall-callerid `1.0.2`

- **Breaking:** `NumberInfo.SpamLevel` enum values renamed — `NONE` → `UNLIKELY`, `TOP` → `CONFIRMED` (`SUSPICIOUS` unchanged)

### trustall-numbersearch `1.0.2`

- **Breaking:** `OnlineNumberInfo.SpamLevel` enum values renamed — `NONE` → `UNLIKELY`, `TOP` → `CONFIRMED` (`SUSPICIOUS` unchanged); integer mapping from Omnidroid API: `0`=UNLIKELY, `1`=SUSPICIOUS, `2`=CONFIRMED

### trustall-offlinedb `1.0.2`

- **Breaking:** `OfflineNumberInfo.SpamLevel` enum values renamed — `NONE` → `UNLIKELY`, `TOP` → `CONFIRMED` (`SUSPICIOUS` unchanged); bit-flag mapping: `0x01`=CONFIRMED, `0x02`=SUSPICIOUS, otherwise UNLIKELY

---

## 2026.03.02 — March 30, 2026 {#trustall-bom-20260302}

<details>
<summary>Module versions in this BOM</summary>

| Module | Version | Gradle Dependency |
|--------|---------|-------------------|
| `trustall-bom` | 2026.03.02 | `com.gogolook.trustall:trustall-bom` |
| `trustall-core` | 2026.03.02 | `com.gogolook.trustall:trustall-core` |
| `trustall-auth` | 1.0.0 | `com.gogolook.trustall:trustall-auth` |
| `trustall-callerid` | 1.0.1 | `com.gogolook.trustall:trustall-callerid` |
| `trustall-calllog` | 1.0.0 | `com.gogolook.trustall:trustall-calllog` |
| `trustall-contact` | 1.0.0 | `com.gogolook.trustall:trustall-contact` |
| `trustall-msgfilter` | 1.0.0 | `com.gogolook.trustall:trustall-msgfilter` |
| `trustall-numberblock` | 1.0.0 | `com.gogolook.trustall:trustall-numberblock` |
| `trustall-numbersearch` | 1.0.1 | `com.gogolook.trustall:trustall-numbersearch` |
| `trustall-offlinedb` | 1.0.1 | `com.gogolook.trustall:trustall-offlinedb` |
| `trustall-permission` | 1.0.0 | `com.gogolook.trustall:trustall-permission` |
| `trustall-smslog` | 1.0.0 | `com.gogolook.trustall:trustall-smslog` |
| `trustall-urlscan` | 1.0.0 | `com.gogolook.trustall:trustall-urlscan` |
| `network:production` | 1.0.0 | `com.gogolook.trustall.network:production` |
| `network:staging` | 1.0.0 | `com.gogolook.trustall.network:staging` |
| `network:sandbox` | 1.0.0 | `com.gogolook.trustall.network:sandbox` |

</details>

### trustall-core `2026.03.02`

- Initial release. SDK entry point; manages initialization, device identity, and configuration.

### trustall-auth `1.0.0`

- Initial release. Device registration and member ID management.

### trustall-callerid `1.0.1`

- Initial release. Caller ID, call event callbacks, and composite number info lookup across contacts, online, and offline sources.

### trustall-calllog `1.0.0`

- Initial release. Call log retrieval and upload to the backend.

### trustall-contact `1.0.0`

- Initial release. Contact lookup by E.164 phone number.

### trustall-msgfilter `1.0.0`

- Initial release. Message classification (spam, promotion, transaction, normal).

### trustall-numberblock `1.0.0`

- Initial release. Local block list management with E.164 normalization.

### trustall-numbersearch `1.0.1`

- Initial release. Online number lookup with configurable cache.

### trustall-offlinedb `1.0.1`

- Initial release. On-device number database with region-based download and incremental update.

### trustall-permission `1.0.0`

- Initial release. Runtime permission request and check helpers used across feature modules.

### trustall-smslog `1.0.0`

- Initial release. SMS and MMS log retrieval with optional time-range filtering.

### trustall-urlscan `1.0.0`

- Initial release. URL threat scanning with configurable cache policy.

### network:production `1.0.0`

- Initial release. Production network variant. Use in production builds.

### network:staging `1.0.0`

- Initial release. Staging network variant. Use for internal staging verification.

### network:sandbox `1.0.0`

- Initial release. Sandbox network variant. Use for development and QA testing.
