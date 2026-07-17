# Release Notes

## Latest Version

[trustall-bom `2026.07.01`](#trustall-bom-20260701)

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
