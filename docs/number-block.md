# Number Block

`Trustall.numberBlock` (`TrustallNumberBlock`) manages the local block list.

## Add / Remove a Block

Use [`add()`](#add) and [`remove()`](#remove):

```kotlin
// Block a number
Trustall.numberBlock.add("+886912345678")

// Unblock a number
Trustall.numberBlock.remove("+886912345678")
```

## Check If a Number Is Blocked

```kotlin
val blocked = Trustall.numberBlock.isBlocked("+886912345678")
```

## Get All Blocked Numbers

[`getAll()`](#getall) returns a list of [`BlockInfo`](#blockinfo):

```kotlin
val list = Trustall.numberBlock.getAll()
list.forEach { info ->
    Log.d("Block", "${info.number} — blocked at ${info.timeMillis}")
}
```

## Clear All Blocks

```kotlin
Trustall.numberBlock.clearAll()
```

---

## API Reference

**Package:** `com.gogolook.trustall.numberblock`

Access via `Trustall.numberBlock`.

### Functions

#### `add`

```kotlin
suspend fun add(number: String)
```

Adds the given number to the block list.

| Parameter | Type | Description |
|-----------|------|-------------|
| `number` | `String` | Phone number to block |

---

#### `remove`

```kotlin
suspend fun remove(number: String)
```

Removes the given number from the block list.

| Parameter | Type | Description |
|-----------|------|-------------|
| `number` | `String` | Phone number to unblock |

---

#### `isBlocked`

```kotlin
suspend fun isBlocked(number: String): Boolean
```

Returns `true` if the given number is blocked.

| Parameter | Type | Description |
|-----------|------|-------------|
| `number` | `String` | Phone number to check |

---

#### `clearAll`

```kotlin
suspend fun clearAll()
```

Removes all numbers from the block list.

---

#### `getAll`

```kotlin
suspend fun getAll(): List<BlockInfo>
```

Returns all blocked numbers.

**Returns:** `List<`[`BlockInfo`](#blockinfo)`>`

---

### BlockInfo

| Field | Type | Description |
|-------|------|-------------|
| `e164` | `String` | E.164 formatted phone number |
| `number` | `String` | Raw phone number |
| `timeMillis` | `Long` | Timestamp when the block was added (Unix milliseconds) |
