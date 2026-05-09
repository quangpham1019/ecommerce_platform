# 0003 - Exception to HTTP Status Mapping

Date: 2026-05-09

Status: Proposed

## Context

The backend uses domain-specific exceptions to represent different failure modes. We need a consistent mapping from those exceptions to HTTP status codes so API consumers get predictable responses and we centralize error handling.

## Decision

Introduce the following domain exceptions and map them to HTTP status codes at the web layer (controller advice / exception handler):

- `ValidationException` → 400 Bad Request
- `BusinessRuleException` → 400 Bad Request
- `InvalidCredentialsException` → 401 Unauthorized
- `ForbiddenOperationException` → 403 Forbidden
- `ResourceNotFoundException` → 404 Not Found
- `ConflictException` → 409 Conflict
- `InsufficientInventoryException` → 409 Conflict

Rationale:

- `ValidationException` and `BusinessRuleException` both represent client-provided data or domain rule violations; 400 communicates the client must change the request.
- Authentication/authorization failures are standard 401/403 responses.
- Not found resources map to 404 to match RESTful semantics.
- Conflicts (uniqueness, inventory) map to 409 to indicate request cannot be completed in current state.

## Consequences

- Add the exception classes (if not present) under `com.quang.marketplace.shared.error` or the appropriate module namespace.
- Implement a `@ControllerAdvice` that translates these exceptions to the corresponding `ResponseEntity`/status with a consistent error payload (timestamp, message, code, path).
- Unit and integration tests should assert on HTTP status codes and error payload shape.

## Next steps

- Create/verify exception classes and their constructors.
- Implement the global exception handler mapping exceptions to statuses.
- Update existing controllers/tests to rely on the handler.
