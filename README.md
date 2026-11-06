# Leegality API Automation Assignment

This project tests the [Restful Booker](https://restful-booker.herokuapp.com) API with Java, REST Assured, Maven, and TestNG. It was created for the Leegality QA Engineer assignment.

The tests cover the health check, authentication, booking search, booking CRUD operations, authorization, malformed requests, and boundary values. Tests create their own bookings instead of relying on the sandbox's seeded records.

## Technology

- Java 17
- Maven
- REST Assured
- TestNG
- Jackson
- REST Assured JSON Schema Validator
- Allure TestNG and Allure REST Assured
- SLF4J and Logback

## Project structure

```text
src/main/java/com/automation
|-- clients       API request classes
|-- config        environment and JVM property configuration
|-- models        booking and authentication models
|-- listeners     Allure failure listener
`-- utils         request specifications and test data

src/main/resources
|-- schemas       booking JSON schemas
`-- logback.xml

src/test/java/com/automation
|-- base          fixture tracking and cleanup
`-- tests         health, authentication, CRUD, and validation tests

testng.xml
BUGS.md
INTERVIEW_NOTES.md
reports/allure-report/index.html
```

## Prerequisites and configuration

Install Java 17 and Maven. The API credentials should be supplied outside the source code:

```powershell
$env:BOOKER_BASE_URL = "https://restful-booker.herokuapp.com"
$env:BOOKER_USERNAME = "<assignment username>"
$env:BOOKER_PASSWORD = "<assignment password>"
```

JVM properties are also supported:

```text
-Dbooker.baseUrl
-Dbooker.username
-Dbooker.password
-Dbooker.connectTimeoutMs
-Dbooker.readTimeoutMs
```

The username and password have no source-code fallback. This prevents credentials from being committed accidentally.

## Running tests

```bash
mvn clean test-compile
mvn clean test
mvn -Dgroups=smoke test
```

The complete suite contains 23 TestNG invocations. Some negative tests are expected to fail because they expose confirmed defects in the public sandbox. Those assertions are intentionally kept strict; see `BUGS.md`.

The suite runs sequentially. Each test creates unique data, records any created booking ID, and attempts cleanup in `@AfterMethod`. Cleanup is best effort so it cannot hide the original test failure.

## Coverage

- `GET /ping` health check.
- `POST /auth` valid, invalid, and missing credentials.
- `GET /booking` list and firstname filtering.
- `GET /booking/{id}` retrieval, missing IDs, and invalid IDs.
- `POST /booking` successful creation and invalid input.
- `PUT /booking/{id}` full update.
- `PATCH /booking/{id}` partial update.
- `DELETE /booking/{id}` deletion and post-delete verification.
- Unauthorized PUT, PATCH, and DELETE requests.
- JSON Schema validation for booking, create-response, and list responses.

## Allure reporting

Generate a report from the latest test results with:

```bash
mvn io.qameta.allure:allure-maven:2.15.1:report
```

The generated report is written to `target/site/allure-maven-plugin/index.html`. A reviewer-readable snapshot from the executed suite is checked in at `reports/allure-report/index.html`.

Request and response attachments are added for API calls. Authentication requests use a separate request specification so credentials and tokens are not written to those generic attachments. Failed tests also receive a failure-context attachment from `FailureListener`.

## Test strategy and limitations

The public sandbox can cold-start, throttle requests, and reset approximately every ten minutes. The client retries one transient 418 or GET 5xx response, but it does not retry business assertion failures. The exact `Accept: application/json` header is used because the sandbox rejects REST Assured's expanded default header with HTTP 418.

The project is focused on functional API checks. It does not perform load, stress, concurrency, or exhaustive query-combination testing. Known API defects and their reproduction evidence are listed in [`BUGS.md`](BUGS.md). Interview-oriented explanations are in [`INTERVIEW_NOTES.md`](INTERVIEW_NOTES.md).
