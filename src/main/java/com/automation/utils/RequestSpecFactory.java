package com.automation.utils;

import com.automation.config.Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecFactory {
    private RequestSpecFactory() {
    }

    public static RequestSpecification bookingSpec() {
        return apiSpec(true);
    }

    public static RequestSpecification bookingSpecWithoutAllure() {
        return apiSpec(false);
    }

    public static RequestSpecification authSpec() {
        return apiSpec(false);
    }

    private static RequestSpecification apiSpec(boolean attachHttpDetails) {
        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", Config.connectTimeoutMs())
                        .setParam("http.socket.timeout", Config.readTimeoutMs())
                        .setParam("http.connection-manager.timeout", (long) Config.readTimeoutMs()));
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(Config.baseUrl())
                .setContentType(ContentType.JSON)
                .setAccept("application/json")
                .setConfig(config);
        if (attachHttpDetails) {
            builder.addFilter(new AllureRestAssured()
                    .setRequestAttachmentName("HTTP request")
                    .setResponseAttachmentName("HTTP response"));
        }
        return builder.build();
    }
}
