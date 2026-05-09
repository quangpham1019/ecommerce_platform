# Authentication & Session Test Plan

This document records the tests added to the codebase and the desired behaviors to verify for authentication, session handling, and related security checks.

## Unit tests
- valid password matches hash: authentication succeeds
- wrong password: authentication fails
- missing/null email or password: validation rejects (input validation)
- disabled user attempts login: authentication fails (user status checks)

## API / Security tests
- valid credentials: returns `Set-Cookie` header (session established)
- wrong email/password: returns generic `invalid credentials` (no leak)
- SQL injection-looking input: no login; no DB corruption
- malformed JSON / invalid content type: returns 400-style error

## Session tests
- successful login then access protected endpoint: access allowed
- failed login then access protected endpoint: access denied
- expired/invalid session cookie: access denied

## Race tests
- login while password is changed: old password should not keep authenticating after change
- repeated failed login attempts: counter/rate limit behaves correctly (if implemented)

## Failure tests
- DB unavailable while loading user: login fails safely; no session created

## Cookie configuration tests
- cookie flags: `HttpOnly`, `Secure`, `SameSite` are set correctly on `Set-Cookie`

## Notes
- Tests were added under `backend/src/test/java/com/quang/marketplace/modules/identity/`.
- Integration tests use Testcontainers MySQL; ensure Docker is available before running integration tests.

## How to run
From the repository root (Windows / PowerShell):

```powershell
cd backend
./mvnw test
```

Or run a single test class:

```powershell
cd backend
./mvnw -Dtest=com.quang.marketplace.modules.identity.AuthSecurityTests test
```

---
Generated and pushed by automation agent.
