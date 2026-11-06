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

import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Feature("Booking CRUD")
public class BookingCrudTest extends BaseTest {
    @Test(groups = {"smoke", "regression", "crud", "contract"}, description = "Create, read, full update, partial update, and delete are independent and persistent")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Exercises the complete booking lifecycle and verifies state after each write.")
    public void completeBookingLifecycleShouldPersistState() throws Exception {
        Booking original = TestDataFactory.validBooking();
        int id = createAndRegisterId(original);

        bookingClient.getBooking(id).then().statusCode(200).body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"));
        bookingClient.getBooking(id).then().statusCode(200).body("firstname", equalTo(original.getFirstname()));

        String token = authClient.getToken();
        Booking fullUpdate = TestDataFactory.validBooking();
        fullUpdate.setFirstname("FullUpdate");
        Response put = bookingClient.updateBooking(id, fullUpdate, token);
        put.then().statusCode(200).body("firstname", equalTo("FullUpdate"), "totalprice", equalTo(fullUpdate.getTotalprice()));
        bookingClient.getBooking(id).then().statusCode(200).body("firstname", equalTo("FullUpdate"));

        Response patch = bookingClient.patchBooking(id, Map.of("lastname", "PatchUpdate"), token);
        patch.then().statusCode(200).body("lastname", equalTo("PatchUpdate"));
        bookingClient.getBooking(id).then().statusCode(200).body("lastname", equalTo("PatchUpdate"));

        bookingClient.deleteBooking(id, token).then().statusCode(201).body(containsString("Created"));
        bookingClient.getBooking(id).then().statusCode(404);
    }

    @Test(groups = {"regression", "crud", "contract"}, description = "Booking creation response matches the documented response schema")
    @Severity(SeverityLevel.CRITICAL)
    public void createResponseShouldMatchSchema() {
        Booking booking = TestDataFactory.validBooking();
        Response response = bookingClient.createBooking(booking);
        response.then().statusCode(200).body(matchesJsonSchemaInClasspath("schemas/booking-create-response-schema.json"));
        registerCreatedBooking(response);
    }

    @Test(groups = {"regression", "crud", "contract"}, description = "Booking list supports firstname filtering and has the expected shape")
    @Severity(SeverityLevel.NORMAL)
    public void bookingListShouldSupportFirstnameFilter() throws Exception {
        Booking booking = TestDataFactory.validBooking();
        int id = createAndRegisterId(booking);
        Response list = bookingClient.listBookingsByFirstname(booking.getFirstname());
        list.then().statusCode(200).body(matchesJsonSchemaInClasspath("schemas/booking-list-schema.json"));
        List<Integer> ids = objectMapper.readTree(list.asString()).findValuesAsText("bookingid")
                .stream().map(Integer::valueOf).toList();
        Assert.assertTrue(ids.contains(id), "Filtered list did not contain the created booking ID " + id);
    }

    @Test(groups = {"regression", "crud", "contract"}, description = "A booking read returns the persisted booking object")
    @Severity(SeverityLevel.NORMAL)
    public void getBookingShouldReturnPersistedFields() {
        Booking booking = TestDataFactory.validBooking();
        int id = createAndRegisterId(booking);
        bookingClient.getBooking(id)
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))
                .body("firstname", equalTo(booking.getFirstname()), "bookingdates.checkin", equalTo(booking.getBookingdates().getCheckin()));
    }

    @Test(groups = {"regression", "negative", "crud"}, description = "A non-existent booking returns not found")
    @Severity(SeverityLevel.NORMAL)
    public void nonExistentBookingShouldReturnNotFound() {
        bookingClient.getBooking(999999999).then().statusCode(404);
    }

    @Test(groups = {"regression", "negative", "crud"}, description = "A non-numeric booking ID returns a client error")
    @Severity(SeverityLevel.NORMAL)
    public void invalidBookingIdShouldReturnClientError() {
        bookingClient.getBooking("not-a-number").then().statusCode(404);
    }

    @DataProvider(name = "protectedOperations")
    public Object[][] protectedOperations() {
        return new Object[][]{{"PUT"}, {"PATCH"}, {"DELETE"}};
    }

    @Test(dataProvider = "protectedOperations", groups = {"regression", "negative", "crud", "authentication"}, description = "Write operations require authentication")
    @Severity(SeverityLevel.CRITICAL)
    public void protectedOperationWithoutAuthenticationShouldBeRejected(String operation) {
        int id = createAndRegisterId(TestDataFactory.validBooking());
        Response response = switch (operation) {
            case "PUT" -> bookingClient.updateBooking(id, TestDataFactory.validBooking(), null);
            case "PATCH" -> bookingClient.patchBooking(id, Map.of("firstname", "NoAuth"), null);
            case "DELETE" -> bookingClient.deleteBooking(id, null);
            default -> throw new IllegalArgumentException("Unsupported operation " + operation);
        };
        Assert.assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                operation + " without authentication returned HTTP " + response.statusCode());
    }

    private void registerCreatedBooking(Response response) {
        try {
            int id = objectMapper.readValue(response.asString(), BookingResponse.class).getBookingid();
            registerCreatedBookingId(id);
            bookingClient.getBooking(id).then().statusCode(200);
        } catch (Exception e) {
            Assert.fail("Could not register created booking: " + e.getMessage());
        }
    }
}
