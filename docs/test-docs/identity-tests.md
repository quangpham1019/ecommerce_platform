# Tests: modules/identity

Location: backend/src/test/java/com/quang/marketplace/modules/identity/

This document enumerates the test classes and individual test cases under `modules/identity` with brief descriptions.

## Files

- [UserRepositoryTest.java](backend/src/test/java/com/quang/marketplace/modules/identity/UserRepositoryTest.java#L1-L200)
  - `should_save_and_find_user_by_email`: verifies `UserRepository.save()` persists and `findByEmail()` returns the saved user.

- [AuthServiceUnitTest.java](backend/src/test/java/com/quang/marketplace/modules/identity/application/AuthServiceUnitTest.java#L1-L200)
  - `register_hashes_password_and_saves`: ensures `register()` encodes the password and saves the user.
  - `register_duplicate_throws`: `register()` throws `BusinessRuleException` when email already exists.
  - `login_invalid_credentials_throw`: `login()` throws on unknown email.
  - `login_wrong_password_throws`: `login()` throws when password mismatch.
  - `login_success_persists_security_context_in_session`: successful login stores Spring Security context in HTTP session.

- [AuthIntegrationTest.java](backend/src/test/java/com/quang/marketplace/modules/identity/AuthIntegrationTest.java#L1-L200)
  - `registerLoginLogoutFlow`: full register → login → logout flow, verifies JSON responses and session handling.
  - `duplicateEmailRegistrationFails`: registering the same email twice fails (DuplicateEmailException surfaced as error).
  - `login_creates_security_context_in_session_and_logout_invalidates`: login populates session; logout invalidates it and subsequent protected endpoints return 401.

- [AuthSecurityTests.java](backend/src/test/java/com/quang/marketplace/modules/identity/AuthSecurityTests.java#L1-L200)
  - `login_returns_set_cookie_header`: login response includes `Set-Cookie` with `JSESSIONID` and `HttpOnly` flag.
  - `malformed_json_or_invalid_content_type_returns_400`: malformed JSON and wrong content-type return 400.
  - `sql_injection_like_input_does_not_login`: attempts with SQL-injection-like payloads do not grant access.
  - `failed_login_then_access_protected_endpoint_is_denied`: failed login should not authenticate the session; protected endpoints return 401.
  - `race_login_while_password_changed_old_password_fails`: simulates concurrent password change; old password no longer works.

- [AuthServiceExceptionTest.java](backend/src/test/java/com/quang/marketplace/modules/identity/AuthServiceExceptionTest.java#L1-L200)
  - `db_unavailable_while_loading_user_throws`: simulates DB unavailable during `login()` and expects a `DataAccessResourceFailureException`.

## Notes

- Integration tests rely on Testcontainers MySQL; Docker must be available to run them.
- Each test class can be executed individually using Maven's `-Dtest=` option.

### Run examples

```powershell
cd backend
./mvnw -Dtest=com.quang.marketplace.modules.identity.AuthIntegrationTest test
./mvnw -Dtest=com.quang.marketplace.modules.identity.AuthSecurityTests test
```
