# Algorithms REST API

A Spring Boot / Java 21 REST service exposing three classic algorithms, built as a portfolio piece to demonstrate REST design, input validation, unit testing, and a clean separation between the API, service, and UI layers.

## Features

Three algorithms, each with its own endpoint and a small browser UI:

| Algorithm                       | Endpoint                                      | Time     | Space    |
| ------------------------------- | --------------------------------------------- | -------- | -------- |
| Merge two sorted integer lists  | `POST /api/algorithms/lists/add`              | O(n+m)   | O(n+m)   |
| Maximum stock profit            | `POST /api/algorithms/stocks/max-profit`      | O(n)     | O(1)     |
| Run-Length Encoding compression | `POST /api/algorithms/strings/rle-compress`   | O(n)     | O(n)     |

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

Then open <http://localhost:8080> — the landing page lists all three algorithms with a *Run* button for each.

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
    └── rleCompress.html

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
