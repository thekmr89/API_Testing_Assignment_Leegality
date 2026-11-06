package com.automation.clients;

import com.automation.utils.RequestSpecFactory;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.function.Supplier;

import static io.restassured.RestAssured.given;

public class BookingClient {
    public Response createBooking(Object booking) {
        return retryTransient(() -> given().spec(RequestSpecFactory.bookingSpec()).body(booking).post("/booking"), false);
    }

    public Response getBooking(int bookingId) {
        return retryTransient(() -> given().spec(RequestSpecFactory.bookingSpec()).get("/booking/{id}", bookingId), true);
    }

    public Response getBooking(String bookingId) {
        return retryTransient(() -> given().spec(RequestSpecFactory.bookingSpec()).get("/booking/{id}", bookingId), true);
    }

    public Response listBookings() {
        return retryTransient(() -> given().spec(RequestSpecFactory.bookingSpec()).get("/booking"), true);
    }

    public Response listBookingsByFirstname(String firstname) {
        return retryTransient(() -> given().spec(RequestSpecFactory.bookingSpec()).queryParam("firstname", firstname).get("/booking"), true);
    }

    public Response updateBooking(int bookingId, Object booking, String token) {
        return authenticatedRequest(token).body(booking).put("/booking/{id}", bookingId);
    }

    public Response patchBooking(int bookingId, Object booking, String token) {
        return authenticatedRequest(token).body(booking).patch("/booking/{id}", bookingId);
    }

    public Response deleteBooking(int bookingId, String token) {
        return authenticatedRequest(token).delete("/booking/{id}", bookingId);
    }

    private RequestSpecification authenticatedRequest(String token) {
        RequestSpecification specification = given().spec(RequestSpecFactory.bookingSpecWithoutAllure());
        if (token != null && !token.isBlank()) {
            specification.cookie("token", token);
        }
        return specification;
    }

    private Response retryTransient(Supplier<Response> request, boolean retryServerError) {
        Response response = request.get();
        boolean transientFailure = response.statusCode() == 418
                || (retryServerError && response.statusCode() >= 500);
        if (transientFailure) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            response = request.get();
        }
        return response;
    }
}
