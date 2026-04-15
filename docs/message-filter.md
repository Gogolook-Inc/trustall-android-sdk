# Message Filter

`Trustall.messageFilter` (`TrustallMessageFilter`) classifies messages into categories such as normal, spam, promotion, or transaction.

## Filter a Single Message

Pass a [`Message`](#message) to [`filter()`](#filter):

```kotlin
val message = Message(key = "msg-001", text = "Congratulations! Click to claim your prize.")

when (val result = Trustall.messageFilter.filter(message)) {
    is FilterResult.Success -> {
        val type = result.results["msg-001"]  // FilterType.SPAM
        Log.d("Filter", "Result: $type")
    }
    is FilterResult.Failure -> {
        Log.e("Filter", "Error", result.error)
    }
}
```

## Batch Filter

Pass a list of [`Message`](#message) objects to [`filter()`](#filter) for batch classification:

```kotlin
val messages = listOf(
    Message(key = "1", text = "Your verification code is 123456"),
    Message(key = "2", text = "Limited offer! 20% off today only"),
    Message(key = "3", text = "Your invoice is ready. View it at our website."),
)

when (val result = Trustall.messageFilter.filter(messages)) {
    is FilterResult.Success -> {
        result.results.forEach { (key, type) ->
            Log.d("Filter", "$key → $type")
        }
    }
    is FilterResult.Failure -> {
        Log.e("Filter", "Error", result.error)
    }
}
```

---

## API Reference

**Package:** `com.gogolook.trustall.msgfilter`

Access via `Trustall.messageFilter`.

### Functions

#### `filter`

```kotlin
suspend fun filter(message: Message): FilterResult
```

Filters a single message.

| Parameter | Type | Description |
|-----------|------|-------------|
| `message` | [`Message`](#message) | The message to classify |

**Returns:** [`FilterResult`](#filterresult)

---

```kotlin
suspend fun filter(messages: List<Message>): FilterResult
```

Filters a batch of messages.

| Parameter | Type | Description |
|-----------|------|-------------|
| `messages` | `List<`[`Message`](#message)`>` | The messages to classify |

**Returns:** [`FilterResult`](#filterresult)

---

### Message

| Field | Type | Description |
|-------|------|-------------|
| `key` | `String` | Custom identifier used to match results |
| `text` | `String` | Message body |

### FilterResult

| Type | Description |
|------|-------------|
| `Success(results)` | Success; `results` is a `Map<String, FilterType>` keyed by `Message.key` |
| `Failure(error)` | Failure; `error` holds the original exception |

### FilterType

| Value | Description |
|-------|-------------|
| `UNKNOWN` | Cannot be determined |
| `NORMAL` | Normal message |
| `SPAM` | Spam |
| `PROMOTION` | Promotional / advertisement |
| `TRANSACTION` | Transactional (bills, OTPs, etc.) |
