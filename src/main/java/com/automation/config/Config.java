package com.automation.config;

public final class Config {
    private Config() {
    }

    public static String baseUrl() {
        return value("booker.baseUrl", "BOOKER_BASE_URL", "https://restful-booker.herokuapp.com");
    }

    public static String username() {
        return value("booker.username", "BOOKER_USERNAME", "");
    }

    public static String password() {
        return value("booker.password", "BOOKER_PASSWORD", "");
    }

    public static int connectTimeoutMs() {
        return integerValue("booker.connectTimeoutMs", "BOOKER_CONNECT_TIMEOUT_MS", 10000);
    }

    public static int readTimeoutMs() {
        return integerValue("booker.readTimeoutMs", "BOOKER_READ_TIMEOUT_MS", 30000);
    }

    private static String value(String property, String environment, String fallback) {
        String propertyValue = System.getProperty(property);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }
        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank() ? fallback : environmentValue;
    }

    private static int integerValue(String property, String environment, int fallback) {
        try {
            return Integer.parseInt(value(property, environment, String.valueOf(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
