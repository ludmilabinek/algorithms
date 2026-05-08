# Algorithms REST API

A Spring Boot / Java 21 REST service exposing nine classic algorithms, built as a portfolio piece to demonstrate REST design, input validation, unit testing, and a clean separation between the API, service, and UI layers.

## Features

Nine algorithms, each with its own endpoint and a small browser UI:

| Algorithm                       | Endpoint                                             | Time          | Space    |
| ------------------------------- | ---------------------------------------------------- | ------------- | -------- |
| Merge two sorted integer lists  | `POST /api/algorithms/lists/add`                     | O(n+m)        | O(n+m)   |
| Maximum stock profit            | `POST /api/algorithms/stocks/max-profit`             | O(n)          | O(1)     |
| Run-Length Encoding compression | `POST /api/algorithms/strings/rle-compress`          | O(n)          | O(n)     |
| Minutes between two times       | `POST /api/algorithms/times/minutes-between`         | O(1)          | O(1)     |
| Most repeated letters in a word | `POST /api/algorithms/strings/most-repeated-letters` | O(n)          | O(n)     |
| No-zero pair                    | `POST /api/algorithms/numbers/no-zero-pair`          | O(n · log n)  | O(1)     |
| Phone validation                | `POST /api/algorithms/phones/validate`               | O(n)          | O(n)     |
| Factorial (big numbers)         | `POST /api/algorithms/numbers/factorial`             | O(n · d)      | O(d)     |
| Magic square check              | `POST /api/algorithms/numbers/magic-square`          | O(n²)         | O(n²)    |

## Tech stack

- **Java 21** (records, modern language features)
- **Spring Boot 4** (Web, Test)
- **Gradle 9** with the wrapper
- **JUnit 5** + **AssertJ** for tests (including `@ParameterizedTest`)
- **Bootstrap 5** + **Alpine.js** (from CDN) for the static UI

## Running locally

```bash
./gradlew bootRun
```

Then open <http://localhost:8080> — the landing page lists all nine algorithms with a *Run* button for each.

## API reference

### Merge two sorted integer lists

Classic merge step from merge sort. Combines two pre-sorted lists into a single sorted list.

```bash
curl -X POST http://localhost:8080/api/algorithms/lists/add \
  -H "Content-Type: application/json" \
  -d '{"list1":[-100,-70,-5,1,2,4,10],"list2":[-88,-13,-5,-1,1,3,4]}'
```

```json
{ "result": [-100,-88,-70,-13,-5,-5,-1,1,1,2,3,4,4,10] }
```

**Validation:** both lists non-null, max 50 elements each, sorted ascending, values in `[-100, 100]`, no null elements.

### Maximum stock profit

Best Time to Buy and Sell Stock (LeetCode #121, with a modification).
Given daily prices, returns the maximum profit from a single buy-then-sell transaction (buy on day *i*, sell on day *j* > *i*).

```bash
curl -X POST http://localhost:8080/api/algorithms/stocks/max-profit \
  -H "Content-Type: application/json" \
  -d '{"prices":[7,1,5,3,6,4]}'
```

```json
{ "result": 5 }
```

**Contract:**
- **positive** — maximum profit achievable
- **`0`** — best possible trade is break-even (no gain, no loss)
- **`-1`** — every possible trade would be a loss (strict loss — the modification vs. the classic problem which returns `0`)

**Validation:** prices non-null, minimum 2 elements, non-negative values, no null elements.

### RLE compression

Run-Length Encoding. Compresses consecutive runs of identical letters into `count+char` pairs.

```bash
curl -X POST http://localhost:8080/api/algorithms/strings/rle-compress \
  -H "Content-Type: application/json" \
  -d '{"text":"aaaAAbbbbBc","caseSensitive":true}'
```

```json
{ "result": "3a2A4b1B1c" }
```

With `"caseSensitive": false`, the same input returns `"5a5b1c"`. When omitted, `caseSensitive` defaults to `true` (handled via a record compact constructor).

**Validation:** text non-null, 1–1000 characters, ASCII letters only (`[a-zA-Z]`). Digits are rejected because the output format `{count}{char}` would be ambiguous to decode otherwise.

### Minutes between two times

Given two times on a 12-hour clock separated by a dash, returns the total minutes between them. If the end time is on or before the start time, it wraps to the next day.

```bash
curl -X POST http://localhost:8080/api/algorithms/times/minutes-between \
  -H "Content-Type: application/json" \
  -d '{"timeRange":"1:00pm-11:00am"}'
```

```json
{ "result": 1320 }
```

**Examples:**
- `"9:00am-10:00am"` → `60` (same-day difference)
- `"1:00pm-11:00am"` → `1320` (22 h — end wraps to the next day)
- `"12:30pm-12:00am"` → `690` (11 h 30 min — end wraps to next midnight)

Input is normalized (uppercased, spaces stripped) before validation, so `"1:00 PM - 11:00 AM"` is accepted.

**Validation:** `timeRange` non-null, matches `^(1[0-2]|0?[1-9]):[0-5][0-9](AM|PM)-(1[0-2]|0?[1-9]):[0-5][0-9](AM|PM)$` (hours `1–12`, minutes `00–59`, each side suffixed with `AM`/`PM`).

### Most repeated letters

Returns the first word in a text whose letters repeat the most. Words are ranked by their letter-count distribution sorted in descending order, compared lexicographically: the word with the most frequent letter wins; ties are broken by the second-most-frequent letter, then the third, and so on. When distributions are fully equal, the earlier word in the text wins.

```bash
curl -X POST http://localhost:8080/api/algorithms/strings/most-repeated-letters \
  -H "Content-Type: application/json" \
  -d '{"text":"Today, is the greatest day ever!"}'
```

```json
{ "result": "greatest" }
```

**Examples:**
- `"Today, is the greatest day ever!"` → `"greatest"` (counts `[2,2,1,1,1,1]` vs `ever`'s `[2,1,1,1]` — tie on the first position, wins on the second)
- `"Hello apple pie"` → `"Hello"` (counts `[2,1,1,1]` tie with `apple`; `Hello` appears earlier)

Letters are counted case-insensitively (`Locale.ROOT`) but the returned word preserves original case. Word boundaries are Unicode-aware: any non-letter character (`\P{L}+`) separates words, so punctuation, whitespace, digits, and tabs all work. Polish, Greek, Cyrillic, and other non-ASCII letters are supported.

**Validation:** `text` non-null, 1–10 000 characters, must contain at least one letter (otherwise there's nothing to rank).

### No-zero pair

Splits a positive integer `n` into two positive integers `a + b = n` such that neither `a` nor `b` contains the digit `0`. Returns the first such pair found while iterating `a` upward from `1`.

```bash
curl -X POST http://localhost:8080/api/algorithms/numbers/no-zero-pair \
  -H "Content-Type: application/json" \
  -d '{"n":1010}'
```

```json
{ "result": [11, 999] }
```

**Examples:**
- `2` → `[1, 1]`
- `11` → `[2, 9]`
- `1010` → `[11, 999]` (`1` and `1009` rejected because `1009` contains `0`; `2` and `1008` rejected; … `11 + 999 = 1010` is the first zero-free pair)

**Validation:** `n` non-null, must be greater than `1` and no greater than `2 147 483 647` (`Integer.MAX_VALUE`). The DTO uses `Long` so values out of range produce a clean validation error instead of a JSON-deserialization failure.

### Phone validation

Validates a Polish 9-digit phone number. Accepts an optional `+48` country prefix and common separators (spaces, hyphens, and parentheses around a 2-digit landline area code). After stripping the separators, the input must consist of exactly 9 digits, optionally preceded by `+48`.

```bash
curl -X POST http://localhost:8080/api/algorithms/phones/validate \
  -H "Content-Type: application/json" \
  -d '{"phone":"+48 500 600 700"}'
```

```json
{ "result": true }
```

**Examples — valid:**
- `"123456789"` → `true` (mobile, no separators)
- `"500 600 700"` → `true` (mobile, spaces)
- `"500-600-700"` → `true` (mobile, hyphens)
- `"(22) 123 45 67"` → `true` (landline, Warsaw — `22` is the area code, part of the 9 digits)
- `"+48 500 600 700"` → `true` (with country prefix)
- `"+48 (22) 123 45 67"` → `true` (international format, landline)

**Examples — invalid:**
- `"12345678"` → `false` (8 digits)
- `"1234567890"` → `false` (10 digits)
- `"+972 500 600 700"` → `false` (foreign country code)
- `"500*600*700"` → `false` (`*` is not an allowed separator)
- `"abcdefghi"` → `false` (no digits)
- `""` → `false` (empty)

**Validation:** `phone` non-null. Anything else (empty string, malformed, wrong length, foreign prefix, illegal characters) returns `{"result": false}` rather than throwing — the endpoint *answers* the validity question rather than rejecting the input.

**Implementation:** strip separators (`[-()\s]`) → match `^(\+48)?[0-9]{9}$`. Both patterns are precompiled at class-load time.

### Factorial (big numbers)

Computes `n!` (the product of all integers from `1` to `n`) for arbitrarily large results — without using `BigInteger` or `BigDecimal`. The result is returned as a decimal string, since even modest inputs overflow `long` (`21!` already exceeds `Long.MAX_VALUE`).

```bash
curl -X POST http://localhost:8080/api/algorithms/numbers/factorial \
  -H "Content-Type: application/json" \
  -d '{"n":100}'
```

```json
{ "result": "933262154439441526816992388562667004907159682643816214685929638952175999932299156089414639761565182862536979208272237582511852109168640000000000000000000000000" }
```

**Examples:**
- `0` → `"1"` (by convention, `0! = 1`)
- `5` → `"120"`
- `20` → `"2432902008176640000"` (the last value that still fits in `long`)
- `25` → `"15511210043330985984000000"` (already beyond `long`)
- `100` → 158 digits
- `5000` → 16 326 digits

**Validation:** `n` non-null, `0 ≤ n ≤ 5000`. The upper limit is a denial-of-service guard — `n = 1 000 000` would allocate millions of digits and block the worker thread.

**Implementation:** schoolbook long multiplication on a digit array (least-significant-first). Each step multiplies the running result by the next factor, propagating carry digit-by-digit. Time complexity is `O(n · d)` where `d` is the number of digits in the final result; space is `O(d)`. The `int` arithmetic stays safe because `digit (≤ 9) × n + carry < 10 · n ≤ 50 000` for `n ≤ 5 000` — well under `Integer.MAX_VALUE`.

### Magic square check

Checks whether an `n × n` matrix is a *magic square* — every row, every column, and both diagonals sum to the same value, **and** the matrix contains each integer from `1` to `n²` exactly once.

```bash
curl -X POST http://localhost:8080/api/algorithms/numbers/magic-square \
  -H "Content-Type: application/json" \
  -d '{"matrix":[[2,7,6],[9,5,1],[4,3,8]]}'
```

```json
{ "result": true }
```

**Examples:**
- `[[1]]` → `true` (trivial 1×1)
- `[[2,7,6],[9,5,1],[4,3,8]]` → `true` (Lo Shu, magic sum 15)
- `[[16,3,2,13],[5,10,11,8],[9,6,7,12],[4,15,14,1]]` → `true` (Dürer's *Melencolia I*, 1514)
- `[[1,2,3],[4,5,6],[7,8,9]]` → `false` (row sums 6/15/24 differ)
- `[[5,5,5],[5,5,5],[5,5,5]]` → `false` (sums match but values are not `{1..n²}`)
- `[[1,2],[3,4]]` → `false` (no 2×2 magic square exists)

**Validation:** `matrix` non-null and non-empty, must be square (`n × n`), every row of equal length, no `null` cells, every cell `> 0`. Capped at `n ≤ 100` as a denial-of-service guard.

**Returns `false` (not throws)** for an otherwise structurally-valid matrix that simply isn't magic — e.g. wrong sums, duplicates, values outside `[1, n²]`.

**Implementation:** single pass over the matrix in `O(n²)` time and `O(n²)` extra space (a `boolean[]` to detect duplicates). On the way through, each row sum is compared against the first row's sum; both diagonals are accumulated by checking `row == col` (main diagonal) and `row == n - col - 1` (anti-diagonal). Numerically safe in `int` for `n ≤ 100` — max sum is `n × n² = 1 000 000`, well below `Integer.MAX_VALUE`.

## Error responses

Every validation failure returns HTTP 400 with a targeted, actionable message:

```json
{ "error": "list2 is not sorted in ascending order (violation at index 3: 7 > 5)" }
```

```json
{ "error": "prices contains value less than 0 at index 2 (got -7)" }
```

```json
{ "error": "text must contain only letters" }
```

Jackson is configured to reject floats where integers are expected (`spring.jackson.deserialization.accept-float-as-int=false`) — `3.14` in an integer array returns 400 instead of being silently truncated to `3`.

## Testing

```bash
./gradlew test
```

Unit tests cover:

- **Happy paths** for each algorithm (including boundary values and parameterized cases)
- **Every validation rule**, asserting the exact error message for each failure
- **Input-immutability contracts** where meaningful (lists and arrays — not Strings, which are immutable by language)
- **Boundary values** (e.g., exactly 50-element lists, values at `±100`, 1000-character text)

## Project structure

```
src/main/java/org/example/algorithms/
├── AlgorithmsApplication.java         Spring Boot entrypoint
├── controller/AlgorithmsController.java  thin HTTP layer
├── service/AlgorithmsService.java     algorithms + validation
├── dto/                               request/response records
└── exception/                         ValidationException + @RestControllerAdvice

src/main/resources/
├── application.properties             Jackson strict-mode config
└── static/                            Bootstrap + Alpine.js UI
    ├── index.html                     landing page listing algorithms
    ├── addSortedLists.html
    ├── maxProfit.html
    ├── rleCompress.html
    ├── minutesBetween.html
    ├── mostRepeatedLetters.html
    ├── noZeroPair.html
    ├── phoneValidate.html
    ├── factorial.html
    └── magicSquare.html

src/test/java/...                      JUnit 5 + AssertJ tests
docs/specs/                            design documents
```

## Design notes

- **Thin controller, service owns logic.** The controller only translates HTTP ↔ DTO; validation and business rules live in the service so they are testable without a Spring context.
- **Global exception handler** maps `ValidationException` to HTTP 400 + a consistent JSON error shape, so new endpoints don't duplicate error-handling code.
- **DTOs as Java records** — minimal boilerplate, clear contracts, Jackson handles them natively. The RLE request uses a compact constructor to default `caseSensitive` to `true` when the field is omitted in JSON.
- **No input mutation.** Algorithms never modify arguments they receive from callers — verified by unit tests for the mutable types (lists, arrays).
- **Algorithm choice is documented.** See [`docs/specs/2026-04-21-add-lists-endpoint.md`](docs/specs/2026-04-21-add-lists-endpoint.md) for the full design document, including *Considered approaches* with trade-offs for the merge-sorted-lists algorithm.

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
