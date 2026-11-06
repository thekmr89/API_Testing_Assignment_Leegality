package com.automation.tests;

import com.automation.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

@Feature("Health")
public class HealthCheckTest extends BaseTest {
    @Test(groups = {"smoke", "regression"}, description = "The booking service responds to its health endpoint")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Checks that the sandbox is awake before API regression is evaluated.")
    public void pingShouldReturnCreatedText() {
        healthClient.ping()
                .then()
                .statusCode(201)
                .body(equalTo("Created"));
    }
}
