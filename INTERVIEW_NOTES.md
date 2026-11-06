# Interview notes

## What the framework does

The project sends requests to the Restful Booker sandbox and keeps request construction separate from test assertions. Tests create their own data, verify the response and persisted state, and clean up any bookings they created.

The main flow is:

1. `Config` reads the base URL, credentials, and timeout values from JVM properties or environment variables.
2. `RequestSpecFactory` creates the common REST Assured specifications.
3. `AuthClient`, `HealthClient`, and `BookingClient` contain endpoint request code.
4. Test classes call those clients and keep the business assertions next to the scenario.
5. `BaseTest` tracks created booking IDs and attempts authenticated cleanup after every test.

## Why REST Assured and TestNG

REST Assured gives the tests readable request and response syntax, path parameters, response-body assertions, and JSON Schema validation without building a custom HTTP wrapper.

TestNG provides the lifecycle hooks, data providers, groups, descriptions, and listener support needed for this assignment. Data providers keep related boundary cases together without duplicating test methods.

## API clients and models

The clients return REST Assured `Response` objects. This keeps assertions in the tests and lets each test choose the checks that matter for its scenario. `Booking`, `BookingDates`, `BookingResponse`, and `AuthResponse` make normal JSON payloads readable. Maps are used only where intentionally malformed values are needed.

`RequestSpecFactory` owns the base URL, JSON headers, timeouts, and Allure filter. Authentication requests do not use the generic HTTP attachment filter, which prevents credentials from being written to the report. Authenticated write requests also use the attachment-free specification so the token cookie is not exposed.

## Authentication

The valid flow posts the configured username and password to `/auth`, reads the returned token with Jackson, and sends it as the `token` cookie for PUT, PATCH, and DELETE. Invalid and missing credentials are tested against the expected 4xx contract. The public API currently returns HTTP 200 for those cases, so those tests intentionally fail and document BUG-004.

## Test data and cleanup

`TestDataFactory` creates unique names with a UUID suffix and uses future dates. This avoids dependence on the sandbox's initial records. Tests run sequentially because the service is shared and resets approximately every ten minutes.

`BaseTest` clears the tracked IDs before each method. After the method, it obtains a token and deletes every ID created by that test. Cleanup is best effort: if authentication or deletion fails, the original test result remains visible.

## Assertions and negative tests

The tests check status codes, response fields, booking IDs, persistence after PUT/PATCH, deletion, authorization, and JSON schemas. Negative tests express the behavior the API should provide: invalid prices and dates, malformed payloads, invalid IDs, and missing authentication should be rejected.

The failing negative tests are intentional. They show that the sandbox accepts negative or zero prices, accepts invalid date ranges, normalizes an invalid calendar date, returns 200 for failed authentication, and returns 500 for malformed booking payloads. The assertions are not weakened to make the build green; the evidence is recorded in `BUGS.md`.

## Allure reporting

`allure-testng` collects TestNG metadata such as feature, severity, description, and failure status. `allure-rest-assured` attaches request and response details for ordinary API calls. `FailureListener` adds the test name and assertion exception when a test fails.

Generate the report with:

```bash
mvn io.qameta.allure:allure-maven:2.15.1:report
```

The generated report is under `target/site/allure-maven-plugin`. The submitted snapshot is `reports/allure-report/index.html`.

## Retry and sandbox behavior

The sandbox can cold-start or briefly return transient 418/5xx responses. `BookingClient` retries once for those conditions; ordinary 4xx responses and assertion failures are not retried. The exact `Accept: application/json` header is deliberate because the sandbox rejects REST Assured's expanded default Accept header with HTTP 418.

## Design choices

The framework uses a few small clients and one shared request-spec factory instead of a generic repository or service layer. That keeps the request code reusable while leaving each assertion visible in the test that owns it. The scope is functional API testing; load and parallel testing would not be appropriate for this shared resettable sandbox.
