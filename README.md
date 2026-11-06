# Leegality API Automation Assignment

Production-style API automation for the [Restful Booker](https://restful-booker.herokuapp.com) sandbox, built as the Leegality QA Engineer take-home submission.

## Overview

The suite covers health, authentication, booking list/filtering, retrieve, create, full update, partial update, and delete. It validates response bodies and persistence, not only status codes, and intentionally keeps correct negative assertions failing when the sandbox accepts invalid business data. The suite contains 23 TestNG scenarios, including data-driven price, payload, date, and authorization boundaries.

## Technology

Java 17, Maven, REST Assured, TestNG, Jackson, REST Assured JSON Schema Validator, Allure TestNG/REST Assured, SLF4J, Logback, and Git.

## Project structure

```text
src/main/java/com/automation
├── clients       # Auth, health, and booking endpoint clients
├── config        # System-property/environment configuration
├── models        # Jackson request/response models
├── listeners     # Allure failure context listener
└── utils         # Request specs and unique test-data factory
src/main/resources
├── schemas       # Booking, create-response, and list schemas
└── logback.xml
src/test/java/com/automation
├── base          # Per-test fixture tracking and cleanup
└── tests         # Health, auth, CRUD, and validation scenarios
testng.xml
BUGS.md
INTERVIEW_NOTES.md
reports/allure-report/index.html
```

## Prerequisites and configuration

Install Java 17 and Maven. Provide the assignment credentials through environment variables without editing source:

```powershell
$env:BOOKER_BASE_URL = "https://restful-booker.herokuapp.com"
$env:BOOKER_USERNAME = "<assignment username>"
$env:BOOKER_PASSWORD = "<assignment password>"
```

Equivalent JVM properties are supported: `-Dbooker.baseUrl`, `-Dbooker.username`, `-Dbooker.password`, `-Dbooker.connectTimeoutMs`, `-Dbooker.readTimeoutMs`, and `-Dbooker.retryAttempts`.

## Running tests

```bash
mvn clean test-compile
mvn clean test
mvn -Dgroups=smoke test
```

The complete run is expected to show failures for confirmed sandbox defects recorded in `BUGS.md`; those assertions are deliberately not weakened. REST Assured attaches HTTP requests and responses to Allure results, excluding authentication requests from the generic body logger so credentials are not exposed.

## Allure report

After a test run, generate the portable HTML report with:

```bash
mvn io.qameta.allure:allure-maven:2.15.1:report
```

Open `target/site/allure-maven-plugin/index.html`. The submitted reviewer-readable snapshot is `reports/allure-report/index.html` and is generated from the executed Allure results. Raw `target/` and transient Allure data are ignored by Git.

## Test strategy and reliability

Each test creates its own uniquely named booking and tracks its ID for teardown. No test depends on the ten preloaded records, test order, or another test's ID. Cleanup is best effort and never replaces the original assertion failure. The client uses bounded retries only for the sandbox's transient cold-start/server-throttle responses; business assertion failures are not retried. Tests run sequentially because the sandbox is shared and resets approximately every ten minutes.

Schema checks cover booking retrieval, creation, and list responses. The suite deliberately does not attempt load testing, concurrency testing, or exhaustive query combinations because those are outside the assignment's risk focus and could disturb the shared sandbox.

## Known limitations

The public sandbox can cold-start, throttle requests, or reset state while a run is in progress. Reported failures should therefore be checked against the attached request/response before triage. The API's invalid-input defects are environment behavior, not framework failures; see `BUGS.md`.

## Deliverables

- Defect evidence and runnable reproduction: [`BUGS.md`](BUGS.md)
- Interview-oriented design notes: [`INTERVIEW_NOTES.md`](INTERVIEW_NOTES.md)
- HTML report: `reports/allure-report/index.html`
