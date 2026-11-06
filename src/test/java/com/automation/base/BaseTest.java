package com.automation.base;

import com.automation.clients.AuthClient;
import com.automation.clients.BookingClient;
import com.automation.clients.HealthClient;
import com.automation.models.Booking;
import com.automation.models.BookingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.testng.Assert.fail;

public abstract class BaseTest {
    protected final AuthClient authClient = new AuthClient();
    protected final BookingClient bookingClient = new BookingClient();
    protected final HealthClient healthClient = new HealthClient();
    protected final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<Integer> createdBookingIds = new LinkedHashSet<>();

    @BeforeMethod(alwaysRun = true)
    public void setUpTest() {
        createdBookingIds.clear();
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpTestData() {
        if (createdBookingIds.isEmpty()) {
            return;
        }
        String token;
        try {
            token = authClient.getToken();
        } catch (RuntimeException ignored) {
            return;
        }
        for (Integer id : createdBookingIds) {
            try {
                bookingClient.deleteBooking(id, token);
            } catch (RuntimeException ignored) {
                // Cleanup must not replace the original test result.
            }
        }
    }

    protected Booking createAndRegister(Booking booking) {
        Response response = bookingClient.createBooking(booking);
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            fail("Fixture creation failed: HTTP " + response.statusCode() + " " + response.asString());
        }
        try {
            BookingResponse created = objectMapper.readValue(response.asString(), BookingResponse.class);
            createdBookingIds.add(created.getBookingid());
        } catch (Exception e) {
            fail("Fixture creation response could not be parsed: " + e.getMessage());
        }
        return booking;
    }

    protected int createAndRegisterId(Booking booking) {
        Response response = bookingClient.createBooking(booking);
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            fail("Fixture creation failed: HTTP " + response.statusCode() + " " + response.asString());
        }
        try {
            int id = objectMapper.readValue(response.asString(), BookingResponse.class).getBookingid();
            createdBookingIds.add(id);
            return id;
        } catch (Exception e) {
            fail("Fixture creation response could not be parsed: " + e.getMessage());
            return -1;
        }
    }

    protected void registerCreatedBookingId(int bookingId) {
        createdBookingIds.add(bookingId);
    }
}
