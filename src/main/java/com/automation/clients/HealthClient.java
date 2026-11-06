package com.automation.clients;

import com.automation.utils.RequestSpecFactory;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class HealthClient {
    public Response ping() {
        return given().spec(RequestSpecFactory.bookingSpec()).get("/ping");
    }
}
