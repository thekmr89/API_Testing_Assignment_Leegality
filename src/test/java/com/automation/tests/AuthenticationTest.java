package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.config.Config;
import com.automation.models.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@Feature("Authentication")
public class AuthenticationTest extends BaseTest {
    @Test(groups = {"smoke", "regression", "authentication"}, description = "Valid credentials return a non-empty token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies the authentication contract needed by protected booking operations.")
    public void validCredentialsShouldReturnToken() throws Exception {
        Response response = authClient.authenticate(Config.username(), Config.password());
        AuthResponse auth = new ObjectMapper().readValue(response.asString(), AuthResponse.class);
        response.then().statusCode(200).body("token", not(emptyOrNullString()));
        org.testng.Assert.assertNotNull(auth.getToken());
    }

    @Test(groups = {"regression", "negative", "authentication"}, description = "Invalid credentials must be rejected with a client error")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Protects against treating an authentication failure as a successful HTTP request.")
    public void invalidCredentialsShouldBeRejected() {
        authClient.authenticate("invalid-user", "invalid-password")
                .then()
                .statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)))
                .body("token", emptyOrNullString());
    }

    @Test(groups = {"regression", "negative", "authentication"}, description = "Missing authentication payload must be rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void missingAuthenticationPayloadShouldBeRejected() {
        authClient.authenticateWithoutBody()
                .then()
                .statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)))
                .body("token", emptyOrNullString());
    }
}
