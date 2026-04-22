# Endpoint: merging two sorted integer lists

**Date:** 2026-04-21
**Status:** implemented

## Context

The `algorithms` project is intended to be a collection of classic algorithms exposed via a REST API — a portfolio piece demonstrating familiarity with Spring Boot 4, Java 21, and solid endpoint-design practices.

First algorithm: **merging two sorted lists** into a single sorted list (the classic *merge* step from merge sort).

## Scope

- `POST /api/algorithms/lists/add` — accepts two sorted integer lists and returns a single sorted list containing all their elements.
- Input validation with clear, actionable error messages.
- Consistent error response shape (`{ "error": "..." }` + HTTP 400).

**Out of scope (for now):**
- Frontend UI / layout
- Unit and integration tests (to be added in the next iteration)
- Authentication / rate limiting
- Pagination / streaming for large lists

## API contract

### Request

```http
POST /api/algorithms/lists/add
Content-Type: application/json

{
  "list1": [-100, -70, -5, 1, 2, 4, 10],
  "list2": [-88, -13, -5, -1, 1, 3, 4]
}
```

### Response 200

```json
{
  "result": [-100, -88, -70, -13, -5, -5, -1, 1, 1, 2, 3, 4, 4, 10]
}
```

### Response 400 (validation)

```json
{
  "error": "list2 is not sorted in ascending order (violation at index 3: 7 > 5)"
}
```

## Validation rules

Each rule returns HTTP 400 with an unambiguous message pointing at the offending list:

1. **Null** — `list1` / `list2` must not be `null`. Empty `[]` is allowed.
   - Message: `"list1 must not be null"`
2. **Size** — at most 50 elements per list.
   - Message: `"list1 exceeds maximum size of 50 (got 73)"`
3. **Order** — each list must be non-decreasing (`a[i] >= a[i-1]`). Duplicates are allowed.
   - Message: `"list2 is not sorted in ascending order (violation at index 3: 7 > 5)"`
4. **Null elements** — lists must not contain `null`.
   - Message: `"list1 must not contain null elements"`
5. **Element range** — every element must satisfy `-100 <= x <= 100`.
   - Message: `"list1 contains value out of range [-100, 100] at index 2 (got 150)"`

## Algorithm — considered approaches

### Chosen: two-pointer merge — O(n+m)

The classic merge step from merge sort. Two pointers walk both lists in parallel, appending the smaller current element to the result.

```
i, j = 0
while i < |L1| && j < |L2|:
    if L1[i] <= L2[j]: result.add(L1[i++])
    else:              result.add(L2[j++])
append remaining L1 from i
append remaining L2 from j
```

**Complexity:** time O(n+m), space O(n+m) (new result list).
**Why:** linear, does not mutate the inputs, needs no defensive copy, symmetric (no branching on which list is longer).

### Considered and rejected

**(A) Insert-in-place: insert shorter list's elements into a copy of the longer one — O(n·m)**

Iterate over the shorter list, find the insertion position in the longer list via `ArrayList.add(index, element)`.

- Rejected because `ArrayList.add(index, ...)` shifts the underlying array — O(n) per insertion, so the full loop becomes O(n·m).
- Also requires a defensive copy of the longer list (to avoid mutating the service's input), which introduces hidden cost and asymmetry in the code.
- For small N (≤50) the practical difference is negligible, but for a portfolio piece the asymptotically correct algorithm was chosen.

**(B) Concatenate and `Collections.sort(...)` — O((n+m) log(n+m))**

Merge both lists into one and sort.

- Rejected because it throws away the key precondition: **the inputs are already sorted**. Two-pointer merge is asymptotically better and exploits the guarantee the endpoint already validates.

**(C) `PriorityQueue` / k-way merge — O((n+m) log k)**

Generalization to k lists.

- Overkill for k=2 and adds priority-queue overhead. Kept in mind for a potential future "merge k lists" endpoint.

## Architecture

Standard Spring MVC in three layers:

```
HTTP
 │
 ▼
AlgorithmsController (/api/algorithms/**)
 │      - parses JSON → DTO
 │      - delegates to the service
 │      - wraps the result into a response DTO
 ▼
AlgorithmsService
 │      - validation (throws ValidationException)
 │      - algorithm logic (two-pointer merge)
 ▼
(pure logic, no external dependencies)

GlobalExceptionHandler (@RestControllerAdvice)
 │      - ValidationException → 400 + { "error": "..." }
```

### Files

| Class | Role |
|-------|------|
| `AlgorithmsApplication` | Spring Boot entrypoint |
| `controller/AlgorithmsController` | REST endpoints; currently `POST /api/algorithms/lists/add` |
| `service/AlgorithmsService` | algorithm logic + validation |
| `dto/AddListsRequest` | record: `{ list1, list2 }` |
| `dto/AddListsResponse` | record: `{ result }` |
| `exception/ValidationException` | domain exception for validation errors |
| `exception/ErrorResponse` | record: `{ error }` — error response shape |
| `exception/GlobalExceptionHandler` | maps exceptions to HTTP status + JSON |

### Design rationale

- **Thin controller** — only orchestrates I/O, no business logic. Keeps the service easy to test in isolation.
- **Service free of Spring-specific dependencies** (beyond `@Service`) — a plain POJO with logic, testable without loading a Spring context.
- **DTOs as `record`** — Java 21, minimum boilerplate, clear API contract, Jackson handles records natively.
- **Global exception handler** — uniform error shape across endpoints. Adding new algorithms does not require duplicating error-handling code.
- **`/api/algorithms` prefix** — leaves room for additional algorithm families (`/api/algorithms/graph/...`, `/api/algorithms/sort/...`, etc.) without colliding with a potential frontend.

## Next steps

- Add a simple HTML/JS layout for manual endpoint testing.
- Integration tests with `MockMvc` covering the happy path and each validation rule.
- Add more algorithms as additional methods on `AlgorithmsService` (or extract into dedicated services once they grow).
- API documentation via OpenAPI / Swagger.
