package com.automation.clients;

import com.automation.config.Config;
import com.automation.models.AuthResponse;
import com.automation.utils.RequestSpecFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthClient {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Response authenticate(String username, String password) {
        return given()
                .spec(RequestSpecFactory.authSpec())
                .body(new Credentials(username, password))
                .post("/auth");
    }

    public Response authenticateWithoutBody() {
        return given().spec(RequestSpecFactory.authSpec()).post("/auth");
    }

    public String getToken() {
        Response response = authenticate(Config.username(), Config.password());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Authentication failed with HTTP " + response.statusCode());
        }
        try {
            return objectMapper.readValue(response.asString(), AuthResponse.class).getToken();
        } catch (Exception e) {
            throw new IllegalStateException("Authentication response did not contain a readable token", e);
        }
    }

    private record Credentials(String username, String password) {
    }
}
