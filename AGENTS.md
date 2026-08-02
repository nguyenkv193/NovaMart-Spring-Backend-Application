# NovaMart Agent Instructions

## Mandatory reading

Before creating or modifying code, read [CONVENTION.md](CONVENTION.md) and apply it to the task. `CONVENTION.md` is the canonical source for naming, architecture, API, security and testing rules.

## Working rules

- Read the relevant source files and existing tests before editing.
- Preserve existing user changes and do not overwrite unrelated work.
- Use `apply_patch` for source and documentation edits.
- Keep the change focused on the requested scope.
- Prefer the existing Spring Boot, JPA, MapStruct, Lombok and security patterns before adding dependencies.
- Keep controllers thin, put business rules in services, and use DTOs at API boundaries.
- Give every public method a clear responsibility, meaningful name and explicit behavior for invalid input/resource absence.
- Use SLF4J logging with parameterized messages. Never use `System.out`, `printStackTrace` or log secrets/tokens/passwords.
- Choose log level intentionally and avoid logging the same exception in multiple layers.
- Apply authorization to every new protected endpoint and test `401`, `403`, success and ownership cases where applicable.
- Do not expose passwords, tokens, secrets or persistence entities in API responses.
- Add or update unit/integration tests for every meaningful behavior change.
- Run the relevant Gradle tests before reporting completion. For the full suite use:

  ```powershell
  .\gradlew.bat test --no-daemon --console=plain --offline
  ```

- Update README/API documentation when an endpoint, response contract or security rule changes.

## Current authorization baseline

- Public: `/api/v1/auth/**` and `GET /api/v1/products/**`.
- `ADMIN`: manage products and access administrative user queries.
- Authenticated user: access their own user profile.
- Future Orders rules must enforce owner access and server-side totals/stock checks.
