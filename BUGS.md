# Confirmed API defects

Observed against `https://restful-booker.herokuapp.com` on 2026-11-06. Each reproduction uses a unique request body and does not depend on preloaded booking IDs. The expected results below represent correct reservation-business behavior.

## BUG-001 — Negative total price is accepted

- **Severity:** High — a booking can be financially invalid and reach downstream billing/reporting.
- **Endpoint:** `POST /booking`
- **Preconditions:** API is reachable; no authentication is required for creation.
- **Reproduction:**

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/booking' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"firstname":"BugProbe","lastname":"NegativePrice","totalprice":-10,"depositpaid":true,"bookingdates":{"checkin":"2026-11-10","checkout":"2026-11-12"}}'
  ```

- **Expected:** HTTP 4xx validation error; no booking is created.
- **Actual:** HTTP 200 and a booking is created with `"totalprice":-10`.
- **Response evidence:** `{"bookingid":4659,"booking":{"firstname":"BugProbe","lastname":"NegativePrice","totalprice":-10,"depositpaid":true,"bookingdates":{"checkin":"2026-11-10","checkout":"2026-11-12"}}}`
- **Related test:** `BookingValidationTest.invalidPriceShouldBeRejected` (`negative price`).

## BUG-002 — Checkout before check-in is accepted

- **Severity:** High — an impossible stay can be persisted and charged or displayed to partners.
- **Endpoint:** `POST /booking`
- **Reproduction:**

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/booking' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"firstname":"BugProbe","lastname":"ReverseDates","totalprice":10,"depositpaid":false,"bookingdates":{"checkin":"2026-11-12","checkout":"2026-11-10"}}'
  ```

- **Expected:** HTTP 4xx validation error; check-out must be after check-in.
- **Actual:** HTTP 200 and the reversed range is stored unchanged.
- **Response evidence:** `{"bookingid":4662,"booking":{"firstname":"BugProbe","lastname":"ReverseDates","totalprice":10,"depositpaid":false,"bookingdates":{"checkin":"2026-11-12","checkout":"2026-11-10"}}}`
- **Related test:** `BookingValidationTest.invalidDateRangeShouldBeRejected` (`checkout before checkin`).

## BUG-003 — Invalid calendar dates are normalized and accepted

- **Severity:** High — a partner-supplied date can silently change to a different reservation date.
- **Endpoint:** `POST /booking`
- **Reproduction:**

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/booking' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"firstname":"BugProbe","lastname":"InvalidCalendar","totalprice":10,"depositpaid":false,"bookingdates":{"checkin":"2026-02-30","checkout":"2026-03-01"}}'
  ```

- **Expected:** HTTP 4xx validation error because February 30 does not exist.
- **Actual:** HTTP 200; `2026-02-30` is silently normalized to `2026-03-02`.
- **Response evidence:** `{"bookingid":4666,"booking":{"firstname":"BugProbe","lastname":"InvalidCalendar","totalprice":10,"depositpaid":false,"bookingdates":{"checkin":"2026-03-02","checkout":"2026-03-01"}}}`
- **Related test:** `BookingValidationTest.invalidDateRangeShouldBeRejected` (`invalid calendar date`).

## BUG-004 — Invalid or missing authentication returns HTTP 200

- **Severity:** High — a successful HTTP status for failed authentication can cause clients, monitoring, or gateways to treat an unauthenticated request as successful.
- **Endpoint:** `POST /auth`
- **Reproduction:**

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/auth' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"username":"wrong","password":"wrong"}'
  ```

- **Expected:** HTTP 401/403 (or another 4xx authentication error), with no token.
- **Actual:** HTTP 200 with `{"reason":"Bad credentials"}` and no token. An empty payload produces the same HTTP 200 response.
- **Related tests:** `AuthenticationTest.invalidCredentialsShouldBeRejected` and `AuthenticationTest.missingAuthenticationPayloadShouldBeRejected`.

## BUG-005 — Malformed booking payloads produce HTTP 500

- **Severity:** Medium — client input errors become server errors, obscuring the cause and creating noisy operational alerts.
- **Endpoint:** `POST /booking`
- **Preconditions:** API is reachable; send a payload with missing required fields, nulls, wrong types, or `{}`.
- **Reproduction:**

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/booking' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{}'
  ```

- **Expected:** HTTP 400 (or another 4xx validation response) with a useful validation message.
- **Actual:** HTTP 500 `Internal Server Error`.
- **Related test:** `BookingValidationTest.malformedBookingPayloadShouldBeRejected` (`empty payload`, plus the missing/null/type variants).
