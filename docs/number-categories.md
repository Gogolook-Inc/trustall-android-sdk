# Number Categories

`bizCategory` and `spamCategory` are string values returned by [Number Search](./number-search.md), [Offline DB](./offline-db.md), and [Caller ID](./caller-id.md) number info APIs.

## Business Categories

`bizCategory` identifies the type of business or entity associated with a number.

| Value | Display Name |
|-------|-------------|
| `automobile` | Automobile |
| `bank` | Financial |
| `beauty` | Personal Care |
| `professional` | Consultants |
| `logistic` | Delivery |
| `education` | Education |
| `entertainment` | Entertainment |
| `food` | Food |
| `government` | Government |
| `life` | Handy Services |
| `health` | Health and Medical |
| `media` | Media |
| `organization` | Company / Organization |
| `others` | Other |
| `publicperson` | Performer or Public Figure |
| `personal` | Personal |
| `pet` | Pets |
| `politics` | Political |
| `shopping` | Shopping |
| `activity` | Exhibitions |
| `traffic` | Transportation |
| `travel` | Travel |

An empty string means no business category has been assigned.

## Spam Categories

`spamCategory` identifies the type of spam associated with a number.

| Value | Description |
|-------|-------------|
| `TOP` | Popular number |
| `TELMARKETING` | Sales / Ads |
| `CALLCENTER` | Customer Service |
| `FRAUD` | Unwelcome |
| `PHISHING` | Phishing |
| `ADULT` | Adult Contents |
| `ILLEGAL` | Illegal Threat |
| `HARASSMENT` | Harassment |
| `ONERING` | One-ring call |
| `HFB` | Frequently Blocked |

An empty string means no spam category has been assigned.
