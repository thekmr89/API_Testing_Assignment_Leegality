# Confirmed API defects

These findings were reproduced against `https://restful-booker.herokuapp.com` on 2026-11-06. The expected results describe normal booking and authentication behavior. The response evidence is from the sandbox; it has not been changed to make the tests pass.

## BUG-001: Negative and zero prices are accepted

- **Severity:** High
- **Endpoint:** `POST /booking`
- **Business impact:** Financially invalid bookings can be stored and passed to downstream billing or reporting.
- **Steps:** Send the following request. No existing booking ID is required.

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/booking' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"firstname":"BugProbe","lastname":"NegativePrice","totalprice":-10,"depositpaid":true,"bookingdates":{"checkin":"2026-11-10","checkout":"2026-11-12"}}'
  ```

- **Expected:** HTTP 4xx validation error and no booking.
- **Actual:** HTTP 200; a booking is created with `"totalprice":-10`. A price of zero was also accepted.
- **Evidence:** `{"bookingid":4659,"booking":{"firstname":"BugProbe","lastname":"NegativePrice","totalprice":-10,"depositpaid":true,"bookingdates":{"checkin":"2026-11-10","checkout":"2026-11-12"}}}`
- **Related test:** `BookingValidationTest.invalidPriceShouldBeRejected`.

## BUG-002: Checkout before or equal to check-in is accepted

- **Severity:** High
- **Endpoint:** `POST /booking`
- **Business impact:** An impossible or zero-length stay can be persisted and shown to customers or partners.
- **Steps:** Send a booking where checkout is not after check-in.

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/booking' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"firstname":"BugProbe","lastname":"ReverseDates","totalprice":10,"depositpaid":false,"bookingdates":{"checkin":"2026-11-12","checkout":"2026-11-10"}}'
  ```

- **Expected:** HTTP 4xx validation error; checkout must be after check-in.
- **Actual:** HTTP 200 and the reversed range is stored unchanged. An equal check-in and checkout date was also accepted.
- **Evidence:** `{"bookingid":4662,"booking":{"firstname":"BugProbe","lastname":"ReverseDates","totalprice":10,"depositpaid":false,"bookingdates":{"checkin":"2026-11-12","checkout":"2026-11-10"}}}`
- **Related test:** `BookingValidationTest.invalidDateRangeShouldBeRejected`.

## BUG-003: Invalid calendar dates are normalized

- **Severity:** High
- **Endpoint:** `POST /booking`
- **Business impact:** A client date can silently become a different reservation date.
- **Steps:** Send February 30 as the check-in date.

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/booking' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"firstname":"BugProbe","lastname":"InvalidCalendar","totalprice":10,"depositpaid":false,"bookingdates":{"checkin":"2026-02-30","checkout":"2026-03-01"}}'
  ```

- **Expected:** HTTP 4xx validation error because February 30 does not exist.
- **Actual:** HTTP 200; `2026-02-30` is normalized to `2026-03-02`.
- **Evidence:** `{"bookingid":4666,"booking":{"firstname":"BugProbe","lastname":"InvalidCalendar","totalprice":10,"depositpaid":false,"bookingdates":{"checkin":"2026-03-02","checkout":"2026-03-01"}}}`
- **Related test:** `BookingValidationTest.invalidDateRangeShouldBeRejected`.

## BUG-004: Invalid or missing authentication returns HTTP 200

- **Severity:** High
- **Endpoint:** `POST /auth`
- **Business impact:** Monitoring or clients may treat failed authentication as a successful request because the HTTP status is 200.
- **Steps:** Send invalid credentials.

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/auth' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"username":"wrong","password":"wrong"}'
  ```

- **Expected:** HTTP 4xx authentication error and no token.
- **Actual:** HTTP 200 with `{"reason":"Bad credentials"}` and no token. An empty payload produced the same status and reason.
- **Related tests:** `AuthenticationTest.invalidCredentialsShouldBeRejected` and `AuthenticationTest.missingAuthenticationPayloadShouldBeRejected`.

## BUG-005: Malformed booking payloads return HTTP 500

- **Severity:** Medium
- **Endpoint:** `POST /booking`
- **Business impact:** Client input errors are reported as server errors, which makes diagnosis harder and can create misleading operational alerts.
- **Steps:** Send an empty object. The same behavior was observed for missing fields, null values, and incorrect data types.

  ```bash
  curl --request POST 'https://restful-booker.herokuapp.com/booking' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{}'
  ```

- **Expected:** HTTP 4xx validation response with a useful message.
- **Actual:** HTTP 500 `Internal Server Error`.
- **Related test:** `BookingValidationTest.malformedBookingPayloadShouldBeRejected`.
