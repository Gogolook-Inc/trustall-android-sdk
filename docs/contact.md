# Contact

`Trustall.contact` (`TrustallContact`) provides contact lookup.

## Permission

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

Call [`requestContactPermission()`](#requestcontactpermission) to request the permission at runtime:

```kotlin
when (Trustall.contact.requestContactPermission(activity)) {
    is PermissionResult.Granted        -> { /* proceed */ }
    is PermissionResult.ShowRationale  -> { /* explain why the permission is needed, then ask again */ }
    is PermissionResult.NeverAskAgain  -> { /* direct the user to system settings */ }
    is PermissionResult.NotSupported   -> { /* permission not supported on this device */ }
}
```

## Look Up Contacts by Number

Pass a phone number in E.164 format to [`queryContacts()`](#querycontacts) (e.g. `"+886912345678"`):

```kotlin
val contacts = Trustall.contact.queryContacts(e164 = "+886912345678")
contacts.forEach { contact ->
    Log.d("Contact", "${contact.displayName} — ${contact.number}")
}
```

## Get All Contacts

[`getAllContacts()`](#getallcontacts) returns all contacts that have at least one phone number:

```kotlin
val all = Trustall.contact.getAllContacts()
```

## Check Permission

```kotlin
val hasPermission = Trustall.contact.hasContactPermission()
```

---

## API Reference

**Package:** `com.gogolook.trustall.contact`

Access via `Trustall.contact`.

### Functions

#### `queryContacts`

```kotlin
suspend fun queryContacts(e164: String): List<Contact>
```

Returns contacts whose phone numbers match the given E.164 number.

| Parameter | Type | Description |
|-----------|------|-------------|
| `e164` | `String` | Phone number in E.164 format (e.g. `"+886912345678"`) |

**Returns:** `List<`[`Contact`](#contact)`>`

---

#### `getAllContacts`

```kotlin
suspend fun getAllContacts(): List<Contact>
```

Returns all contacts that have at least one phone number.

**Returns:** `List<`[`Contact`](#contact)`>`

---

#### `hasContactPermission`

```kotlin
fun hasContactPermission(): Boolean
```

Returns `true` if `READ_CONTACTS` is granted.

---

#### `requestContactPermission`

```kotlin
suspend fun requestContactPermission(activity: ComponentActivity): PermissionResult
```

Requests `READ_CONTACTS` permission.

| Parameter | Type | Description |
|-----------|------|-------------|
| `activity` | `ComponentActivity` | The activity used to show the permission dialog |

**Returns:** [`PermissionResult`](#permissionresult)

---

### Contact

| Field | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Contact ID |
| `number` | `String` | Raw phone number |
| `e164` | `String` | E.164 formatted phone number |
| `displayName` | `String` | Display name |
| `lookupKey` | `String` | System contact lookup key |

### PermissionResult

| Type | Description |
|------|-------------|
| `Granted` | Permission is granted |
| `ShowRationale` | Should show rationale before asking again |
| `NeverAskAgain` | User denied permanently — direct them to system settings |
| `NotSupported` | Permission not supported on this device |
