package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.models.Booking;
import com.automation.models.BookingResponse;
import com.automation.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

@Feature("Booking validation")
public class BookingValidationTest extends BaseTest {
    @DataProvider(name = "invalidPrices")
    public Object[][] invalidPrices() {
        return new Object[][]{{"negative price", -1}, {"zero price", 0}};
    }

    @Test(dataProvider = "invalidPrices", groups = {"regression", "negative"}, description = "Negative and zero booking prices are rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Guards the production incident scenario where an invalid total price was accepted.")
    public void invalidPriceShouldBeRejected(String label, int price) {
        Booking booking = TestDataFactory.validBooking();
        booking.setTotalprice(price);
        Response response = bookingClient.createBooking(booking);
        registerIfCreated(response);
        response.then().statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
    }

    @DataProvider(name = "invalidPayloads")
    public Object[][] invalidPayloads() {
        Map<String, Object> missingField = new LinkedHashMap<>();
        missingField.put("firstname", "Missing");
        missingField.put("totalprice", 10);
        missingField.put("depositpaid", false);
        missingField.put("bookingdates", Map.of("checkin", "2026-11-10", "checkout", "2026-11-12"));

        Map<String, Object> nullValue = new LinkedHashMap<>();
        nullValue.put("firstname", null);
        nullValue.put("lastname", null);
        nullValue.put("totalprice", null);
        nullValue.put("depositpaid", null);
        nullValue.put("bookingdates", null);

        Map<String, Object> wrongType = new LinkedHashMap<>();
        wrongType.put("firstname", 123);
        wrongType.put("lastname", true);
        wrongType.put("totalprice", "10");
        wrongType.put("depositpaid", "false");
        wrongType.put("bookingdates", "not-an-object");

        return new Object[][]{{"missing required field", missingField}, {"null values", nullValue},
                {"incorrect data types", wrongType}, {"empty payload", Map.of()}};
    }

    @Test(dataProvider = "invalidPayloads", groups = {"regression", "negative"}, description = "Malformed booking payloads produce a client validation error")
    @Severity(SeverityLevel.CRITICAL)
    public void malformedBookingPayloadShouldBeRejected(String label, Map<String, Object> payload) {
        bookingClient.createBooking(payload).then().statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
    }

    @DataProvider(name = "invalidDateRanges")
    public Object[][] invalidDateRanges() {
        return new Object[][]{
                {"checkout before checkin", "2026-11-12", "2026-11-10"},
                {"checkout equal to checkin", "2026-11-10", "2026-11-10"},
                {"malformed date", "not-a-date", "2026-11-12"},
                {"invalid calendar date", "2026-02-30", "2026-03-01"}
        };
    }

    @Test(dataProvider = "invalidDateRanges", groups = {"regression", "negative"}, description = "Invalid booking date ranges are rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Catches both chronological and calendar-date validation defects.")
    public void invalidDateRangeShouldBeRejected(String label, String checkin, String checkout) {
        Booking booking = TestDataFactory.validBooking();
        booking.getBookingdates().setCheckin(checkin);
        booking.getBookingdates().setCheckout(checkout);
        Response response = bookingClient.createBooking(booking);
        registerIfCreated(response);
        Assert.assertTrue(response.statusCode() >= 400 && response.statusCode() < 500,
                label + " returned HTTP " + response.statusCode() + " with body " + response.asString());
    }

    private void registerIfCreated(Response response) {
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            try {
                registerCreatedBookingId(objectMapper.readValue(response.asString(), BookingResponse.class).getBookingid());
            } catch (Exception ignored) {
                // The assertion below will report an unusable response.
            }
        }
    }
}
