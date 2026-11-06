# Interview notes

## Architecture

`Config` owns environment/system-property overrides and safe defaults. `RequestSpecFactory` centralizes the base URI, JSON headers, timeouts, and Allure REST filter. `AuthClient`, `HealthClient`, and `BookingClient` contain endpoint request construction only; tests own assertions. Jackson models keep normal booking payloads readable, while maps are used deliberately for malformed-type cases.

`BaseTest` creates a fresh ID set before each method and performs best-effort authenticated cleanup afterward. `TestDataFactory` creates unique names and future dates, so tests do not depend on the sandbox's seeded records. No parallel execution is configured because this is a shared, self-resetting service.

## REST Assured and authentication

Clients use `given().spec(...)` and reusable path/query parameters. The valid auth flow posts credentials to `/auth`, reads the token with Jackson, and sends it as `Cookie: token=<token>` for PUT, PATCH, and DELETE. Authentication requests use a separate request spec without the generic Allure body attachment, preventing credential leakage. Negative auth tests assert the correct business contract even though the API returns HTTP 200.

## TestNG lifecycle and data providers

`@BeforeMethod` clears fixture state; `@AfterMethod(alwaysRun = true)` deletes any created IDs. TestNG data providers cover negative/zero prices, malformed payloads, invalid dates, and the three protected write operations without copy-pasted test methods. Groups include `smoke`, `regression`, `authentication`, `crud`, `negative`, and `contract`.

## Assertions and schemas

Assertions are kept in test classes so each test communicates its business intent. They check status, response body fields, persistence after PUT/PATCH, deletion behavior, and non-existent IDs. JSON Schema Validator checks the booking object, create response, and list response. The schemas require nested booking dates and correct primitive types.

## Reporting

`allure-rest-assured` attaches sanitized request/response HTML to each API call, and `FailureListener` adds the test name, description, and assertion context on failure. Allure annotations provide feature, severity, description, and navigable grouping. The generated HTML report is checked into `reports/allure-report/index.html`; raw results remain build output.

## Retry trade-off

The public service can cold-start or return transient 418/5xx responses. `BookingClient` performs at most one reissue for those transient conditions; ordinary 4xx business responses and assertion failures are never retried. This keeps the retry policy narrow and avoids turning an invalid-data test into a second business operation. The shared request spec uses the exact `Accept: application/json` header because the sandbox rejects REST Assured's expanded content-type Accept list with a misleading 418.

## Defects found

The suite confirms negative and zero prices are accepted, reversed/equal date ranges are accepted, invalid calendar dates are normalized, malformed booking payloads return HTTP 500 instead of a client validation error, and invalid/missing auth returns HTTP 200. The most consequential findings are detailed with cURL and evidence in `BUGS.md`.

## Design trade-offs

The framework intentionally avoids a large generic abstraction layer: clients are small and explicit, while common concerns live in one request-spec factory and one fixture base. It does not perform load testing or run in parallel because those would add little signal for this assignment and could contaminate the shared sandbox. A failing test for a known defect is preserved as a regression guard rather than marked passed.
